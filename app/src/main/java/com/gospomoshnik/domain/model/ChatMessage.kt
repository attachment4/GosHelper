package com.gospomoshnik.domain.model

/**
 * Одно сообщение в диалоге.
 * role: "user" | "assistant"
 */
data class ChatMessage(
    val id: Long = 0,
    val sessionId: Long,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
