package com.gospomoshnik.domain.model

import androidx.compose.runtime.Immutable

/** Бесплатный лимит — вопросов в день. */
const val FREE_DAILY_LIMIT = 3

/**
 * Статус подписки пользователя.
 * Хранится локально в DataStore; верифицируется через FastAPI бэкенд.
 */
@Immutable
data class SubscriptionStatus(
    val isPro: Boolean = false,
    val requestsUsed: Int = 0,                    // счётчик за сегодня
    val requestsLimit: Int = FREE_DAILY_LIMIT,    // 3/день для Free, MAX для Pro
    val expiresAt: Long? = null                   // Unix-ms окончания Pro; null — Free
) {
    val requestsLeft: Int get() = (requestsLimit - requestsUsed).coerceAtLeast(0)
    val canSendMessage: Boolean get() = isPro || requestsLeft > 0
}
