package com.doot.app.connection

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/**
 * Pipe-delimited protocol over TCP:
 *   STATUS|RECORDING
 *   STATUS|IDLE
 *   MESSAGE|<lang>|<text>
 *   ALERT|<lang>|<text>
 */
data class TransportPacket(
    val type: String,       // "STATUS", "MESSAGE", "ALERT"
    val lang: String,       // language code or status payload
    val text: String        // message text (empty for STATUS packets)
)

class SocketTransport {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private val _incoming = MutableSharedFlow<TransportPacket>()
    val incoming: Flow<TransportPacket> = _incoming.asSharedFlow()

    private val writeMutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var readJob: Job? = null

    companion object {
        private const val TAG = "SocketTransport"
    }

    fun attach(newSocket: Socket) {
        close()
        socket = newSocket
        try {
            // PrintWriter with autoFlush=true, but we still call flush() explicitly
            writer = PrintWriter(newSocket.getOutputStream(), true)
            reader = BufferedReader(InputStreamReader(newSocket.getInputStream(), Charsets.UTF_8))

            readJob = scope.launch {
                startReadLoop()
            }
            Log.d(TAG, "Socket attached, read loop started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach socket", e)
            close()
        }
    }

    /**
     * Send a STATUS update (RECORDING / IDLE).
     */
    suspend fun sendStatus(status: String) {
        sendRaw("STATUS|$status")
    }

    /**
     * Send a SPEECH message with language and text.
     */
    suspend fun sendMessage(lang: String, text: String) {
        sendRaw("MESSAGE|$lang|$text")
    }

    /**
     * Send an ALERT message with language and text.
     */
    suspend fun sendAlert(lang: String, text: String) {
        sendRaw("ALERT|$lang|$text")
    }

    private suspend fun sendRaw(line: String) {
        val w = writer ?: return
        try {
            writeMutex.withLock {
                withContext(Dispatchers.IO) {
                    w.println(line)
                    w.flush()
                    Log.d(TAG, "SENT: $line")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Send failed", e)
        }
    }

    private suspend fun startReadLoop() = withContext(Dispatchers.IO) {
        val r = reader ?: return@withContext
        try {
            while (isActive) {
                val line = r.readLine() ?: break  // null = socket closed
                Log.d(TAG, "RECV: $line")

                val parts = line.split("|", limit = 3)
                val packet = when {
                    parts.size >= 2 && parts[0] == "STATUS" -> {
                        // STATUS|RECORDING  or  STATUS|IDLE
                        TransportPacket("STATUS", parts[1], "")
                    }
                    parts.size >= 3 && parts[0] == "MESSAGE" -> {
                        // MESSAGE|en|hello world
                        TransportPacket("MESSAGE", parts[1], parts[2])
                    }
                    parts.size >= 3 && parts[0] == "ALERT" -> {
                        // ALERT|en|Emergency!
                        TransportPacket("ALERT", parts[1], parts[2])
                    }
                    else -> {
                        Log.w(TAG, "Unknown packet format: $line")
                        null
                    }
                }

                if (packet != null) {
                    // Emit on the shared flow; collectors on Main will pick it up
                    _incoming.emit(packet)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Read loop failed/terminated", e)
        } finally {
            close()
        }
    }

    fun close() {
        readJob?.cancel()
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        writer = null
        reader = null
    }
}
