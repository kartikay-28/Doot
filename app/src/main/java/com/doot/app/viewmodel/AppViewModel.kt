package com.doot.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.doot.app.AppState
import com.doot.app.audio.AlertPlayer
import com.doot.app.audio.AudioCapture
import com.doot.app.connection.ConnectionManager
import com.doot.app.connection.ConnectionState
import com.doot.app.connection.SocketTransport
import com.doot.app.speech.SttEngine
import com.doot.app.speech.TtsEngine
import com.doot.app.utils.TranslationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val connectionManager = ConnectionManager(application)
    private val socketTransport = SocketTransport()
    private var activeServerSocket: ServerSocket? = null
    private val audioCapture = AudioCapture()
    private val sttEngine = SttEngine(application)
    private val ttsEngine = TtsEngine(application)
    private val alertPlayer = AlertPlayer(application)

    val connectionState = connectionManager.state
    val discoveredPeers = connectionManager.discoveredPeers

    private val _appState = MutableStateFlow<AppState>(AppState.Disconnected)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _myTranscript = MutableStateFlow("")
    val myTranscript: StateFlow<String> = _myTranscript.asStateFlow()

    private val _receivedTranscript = MutableStateFlow("")
    val receivedTranscript: StateFlow<String> = _receivedTranscript.asStateFlow()

    // ── Language: this is the TARGET language the user wants to HEAR translations in ──
    private val _selectedLang = MutableStateFlow("en")
    val selectedLang: StateFlow<String> = _selectedLang.asStateFlow()

    // ── Peer status: shows "RECORDING" / "IDLE" from the remote device ──
    private val _peerStatus = MutableStateFlow("IDLE")
    val peerStatus: StateFlow<String> = _peerStatus.asStateFlow()

    init {
        // Observe connection state
        viewModelScope.launch {
            connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Connected -> {
                        _appState.value = AppState.Connected
                        setupSocket()
                    }
                    is ConnectionState.Idle, is ConnectionState.Failed -> {
                        _appState.value = AppState.Disconnected
                        _peerStatus.value = "IDLE"
                        socketTransport.close()
                        try {
                            activeServerSocket?.close()
                        } catch (e: Exception) {}
                        activeServerSocket = null
                    }
                    else -> {}
                }
            }
        }

        // Observe incoming packets from the pipe-delimited protocol
        viewModelScope.launch {
            socketTransport.incoming.collect { packet ->
                // This collect runs on Main by default (viewModelScope)
                handleIncomingPacket(packet)
            }
        }

        // Initialize models
        viewModelScope.launch(Dispatchers.IO) {
            sttEngine.loadModel(_selectedLang.value)
            ttsEngine.loadModel(_selectedLang.value)
        }
    }

    private fun setupSocket() {
        val groupInfo = connectionManager.getGroupInfo() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Ensure any old server socket is closed before creating a new one
                activeServerSocket?.close()
                activeServerSocket = null

                val socket = if (groupInfo.isGroupOwner) {
                    val serverSocket = ServerSocket(8988)
                    serverSocket.reuseAddress = true
                    activeServerSocket = serverSocket
                    serverSocket.accept()
                } else {
                    var connectedSocket: Socket? = null
                    for (i in 1..5) {
                        try {
                            connectedSocket = Socket(groupInfo.groupOwnerAddress.hostAddress, 8988)
                            break
                        } catch (e: Exception) {
                            if (i == 5) throw e
                            kotlinx.coroutines.delay(1000)
                        }
                    }
                    connectedSocket!!
                }
                socketTransport.attach(socket)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _appState.value = AppState.Disconnected
                }
                connectionManager.disconnect()
            }
        }
    }

    // ── Handle incoming TransportPacket on Main thread ──
    private suspend fun handleIncomingPacket(packet: com.doot.app.connection.TransportPacket) {
        when (packet.type) {
            "STATUS" -> {
                // Peer pressed/released PTT → update peer status for UI
                _peerStatus.value = packet.lang  // "RECORDING" or "IDLE"
            }

            "MESSAGE" -> {
                val originalText = packet.text
                val sourceLang = packet.lang   // language the sender spoke in (e.g. "en")

                if (originalText.isBlank()) return

                // ── Translation: translate from source language to OUR selected language ──
                val targetLang = _selectedLang.value
                val displayText = if (sourceLang != targetLang && sourceLang == "en") {
                    // English → Hindi/Punjabi translation via demo dictionary
                    val translated = TranslationEngine.translateForDemo(originalText, targetLang)
                    "$originalText\n→ $translated"
                } else {
                    originalText
                }

                _receivedTranscript.value = displayText
                _appState.value = AppState.Receiving

                // ── TTS: speak the translated text in our selected language ──
                val ttsLang = targetLang
                if (com.doot.app.speech.ModelAssets.isLanguageInstalled(getApplication(), ttsLang)) {
                    val ttsText = if (sourceLang != targetLang && sourceLang == "en") {
                        TranslationEngine.translateForDemo(originalText, targetLang)
                    } else {
                        originalText
                    }

                    withContext(Dispatchers.IO) {
                        ttsEngine.loadModel(ttsLang)
                    }
                    val audioSamples = withContext(Dispatchers.IO) {
                        ttsEngine.synthesize(ttsText)
                    }

                    _appState.value = AppState.Speaking
                    alertPlayer.playNormal(audioSamples, ttsEngine.sampleRate)
                } else {
                    _receivedTranscript.value = displayText + "\n(Voice model not installed)"
                }

                _appState.value = AppState.Connected
            }

            "ALERT" -> {
                val alertText = packet.text
                _receivedTranscript.value = "⚠ ALERT: $alertText"
                _appState.value = AppState.Receiving

                val ttsLang = _selectedLang.value
                val ttsText = if (packet.lang == "en" && ttsLang != "en") {
                    TranslationEngine.translateForDemo(alertText, ttsLang)
                } else {
                    alertText
                }

                if (com.doot.app.speech.ModelAssets.isLanguageInstalled(getApplication(), ttsLang)) {
                    withContext(Dispatchers.IO) {
                        ttsEngine.loadModel(ttsLang)
                    }
                    val audioSamples = withContext(Dispatchers.IO) {
                        ttsEngine.synthesize(ttsText)
                    }
                    _appState.value = AppState.Speaking
                    alertPlayer.playAlert(audioSamples, ttsEngine.sampleRate)
                }
                _appState.value = AppState.Connected
            }
        }
    }

    fun onConnectClicked() {
        connectionManager.startDiscoveryAndConnect()
    }

    fun onDisconnectClicked() {
        connectionManager.disconnect()
    }

    fun onPeerClicked(device: android.net.wifi.p2p.WifiP2pDevice) {
        connectionManager.connectToPeer(device)
    }

    fun onTalkPressed() {
        _appState.value = AppState.Recording
        audioCapture.start()
        // ── Send STATUS|RECORDING to peer so they see our live status ──
        viewModelScope.launch(Dispatchers.IO) {
            socketTransport.sendStatus("RECORDING")
        }
    }

    fun onTalkReleased() {
        // ── Send STATUS|IDLE to peer ──
        viewModelScope.launch(Dispatchers.IO) {
            socketTransport.sendStatus("IDLE")
        }

        if (!com.doot.app.speech.ModelAssets.isLanguageInstalled(getApplication(), _selectedLang.value)) {
            _appState.value = AppState.Connected
            _myTranscript.value = "(Model not installed. Download it first!)"
            return
        }

        viewModelScope.launch {
            _appState.value = AppState.Transcribing
            val samples = audioCapture.stop()

            val text = withContext(Dispatchers.IO) {
                sttEngine.loadModel(_selectedLang.value)
                sttEngine.recognize(samples)
            }

            if (text.isNotBlank()) {
                _myTranscript.value = text
                _appState.value = AppState.Sending
                // ── Send MESSAGE|<lang>|<text> over the wire ──
                withContext(Dispatchers.IO) {
                    socketTransport.sendMessage(_selectedLang.value, text)
                }
            } else {
                _myTranscript.value = "(Didn't catch that)"
            }

            _appState.value = AppState.Connected
        }
    }

    fun onAlertClicked() {
        viewModelScope.launch {
            _myTranscript.value = "⚠ ALERT SENT"
            _appState.value = AppState.Sending
            withContext(Dispatchers.IO) {
                socketTransport.sendAlert(
                    _selectedLang.value,
                    "Emergency Alert! Please respond immediately."
                )
            }
            _appState.value = AppState.Connected
        }
    }

    fun onLangToggle(lang: String) {
        _selectedLang.value = lang
        viewModelScope.launch(Dispatchers.IO) {
            sttEngine.loadModel(lang)
            ttsEngine.loadModel(lang)
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectionManager.disconnect()
        socketTransport.close()
        try {
            activeServerSocket?.close()
        } catch (e: Exception) {}
        activeServerSocket = null
        sttEngine.release()
        ttsEngine.release()
    }
}
