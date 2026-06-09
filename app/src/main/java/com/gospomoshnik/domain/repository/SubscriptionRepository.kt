package com.gospomoshnik.domain.repository

import com.gospomoshnik.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория подписки.
 * Реализация — DataStore (локально) + FastAPI (верификация).
 */
interface SubscriptionRepository {

    /** Реактивный поток статуса подписки. */
    fun getStatus(): Flow<SubscriptionStatus>

    /** Увеличить счётчик использованных запросов на 1. */
    suspend fun incrementUsage()

    /** Сбросить счётчик в начале нового месяца. */
    suspend fun resetMonthlyUsage()

    /** Активировать Pro после успешной оплаты. expiresAt — Unix-timestamp. */
    suspend fun activatePro(expiresAt: Long)

    /** Отменить Pro (по истечении или после отмены подписки). */
    suspend fun deactivatePro()
}
