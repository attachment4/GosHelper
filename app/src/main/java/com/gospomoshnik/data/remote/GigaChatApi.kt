package com.gospomoshnik.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GigaChatApi {

    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") bearer: String,
        @Body request: GigaChatRequest
    ): GigaChatResponse
}

// ── Request ──────────────────────────────────────────────────────────────────

data class GigaChatRequest(
    val model: String = "GigaChat",
    val messages: List<GigaChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1024,
    val stream: Boolean = false
)

data class GigaChatMessage(
    val role: String,   // "system" | "user" | "assistant"
    val content: String
)

// ── Response ─────────────────────────────────────────────────────────────────

data class GigaChatResponse(
    val choices: List<Choice>
) {
    val text: String get() = choices.firstOrNull()?.message?.content.orEmpty()
}

data class Choice(
    val message: GigaChatMessage,
    val finish_reason: String?
)
