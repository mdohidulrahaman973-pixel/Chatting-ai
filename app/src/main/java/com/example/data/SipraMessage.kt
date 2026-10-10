package com.example.data

import java.util.UUID

enum class MessageSender {
    USER,
    SIPRA
}

data class SipraMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpeaking: Boolean = false,
    val isError: Boolean = false
)
