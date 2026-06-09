package com.gospomoshnik.domain.model

/**
 * Статус подписки пользователя.
 * Хранится локально в DataStore; верифицируется через FastAPI бэкенд.
 */
data class SubscriptionStatus(
    val isPro: Boolean = false,
    val requestsUsed: Int = 0,          // счётчик в текущем месяце
    val requestsLimit: Int = 10,        // 10 для Free, Int.MAX_VALUE для Pro
    val expiresAt: Long? = null         // Unix-timestamp окончания Pro; null — Free
) {
    val requestsLeft: Int get() = (requestsLimit - requestsUsed).coerceAtLeast(0)
    val canSendMessage: Boolean get() = isPro || requestsLeft > 0
}
