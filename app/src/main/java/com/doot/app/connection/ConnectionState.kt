package com.doot.app.connection

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Discovering : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val peerName: String) : ConnectionState()
    data class Failed(val error: String) : ConnectionState()
}
