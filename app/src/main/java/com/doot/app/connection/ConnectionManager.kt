package com.doot.app.connection

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionManager(private val context: Context) {
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val discoveredPeers: StateFlow<List<WifiP2pDevice>> = _discoveredPeers.asStateFlow()

    private var groupInfo: WifiP2pInfo? = null

    init {
        channel = manager?.initialize(context, context.mainLooper, null)
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList: WifiP2pDeviceList ->
        val peers = peerList.deviceList.toList()
        if (_state.value is ConnectionState.Discovering) {
            _discoveredPeers.value = peers
        }
    }

    private val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { info ->
        groupInfo = info
        if (info.groupFormed) {
            _state.value = ConnectionState.Connected("Peer")
        } else {
            _state.value = ConnectionState.Idle
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscoveryAndConnect() {
        if (_state.value is ConnectionState.Connected || _state.value is ConnectionState.Connecting) return
        _state.value = ConnectionState.Discovering
        
        registerReceiver()
        
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reasonCode: Int) {
                _state.value = ConnectionState.Failed("Discovery failed: $reasonCode")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun connectToPeer(device: WifiP2pDevice, retryCount: Int = 0) {
        _state.value = ConnectionState.Connecting
        
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }
        
        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reason: Int) {
                if ((reason == WifiP2pManager.ERROR || reason == WifiP2pManager.BUSY) && retryCount < 3) {
                    // Hardware collision: wait a random time to break the tie, then retry
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (_state.value is ConnectionState.Connecting) {
                            connectToPeer(device, retryCount + 1)
                        }
                    }, (300..1200).random().toLong())
                } else {
                    _state.value = ConnectionState.Failed("Connection failed: $reason")
                }
            }
        })
    }

    fun getGroupInfo(): WifiP2pInfo? = groupInfo

    fun disconnect() {
        manager?.cancelConnect(channel, null)
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _state.value = ConnectionState.Idle
            }
            override fun onFailure(reason: Int) { }
        })
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (receiver != null) return
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        }
        receiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        manager?.requestPeers(channel, peerListListener)
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        manager?.requestConnectionInfo(channel, connectionInfoListener)
                    }
                }
            }
        }
        context.registerReceiver(receiver, intentFilter)
    }

    private fun unregisterReceiver() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
        }
    }
}
