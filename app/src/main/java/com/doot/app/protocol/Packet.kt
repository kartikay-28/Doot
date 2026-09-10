package com.doot.app.protocol

import kotlinx.serialization.Serializable

@Serializable
data class Packet(
    val type: String,      // "SPEECH" | "ALERT" | "PING"
    val lang: String,      // "en" | "hi" | "pa"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
