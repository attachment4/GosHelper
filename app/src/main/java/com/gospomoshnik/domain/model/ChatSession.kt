package com.gospomoshnik.domain.model

import androidx.compose.runtime.Immutable

/**
 * Доменная модель сессии чата.
 * Не зависит от Android / Room / Retrofit — чистый Kotlin.
 */
@Immutable
data class ChatSession(
    val id: Long = 0,
    val category: String,       // "gibdd" | "zhkh" | "labor" | "benefits" | "court" | "documents"
    val title: String,          // автозаголовок из первого вопроса пользователя
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
