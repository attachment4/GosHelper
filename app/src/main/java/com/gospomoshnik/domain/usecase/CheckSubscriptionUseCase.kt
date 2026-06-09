package com.gospomoshnik.domain.usecase

import com.gospomoshnik.domain.model.SubscriptionStatus
import com.gospomoshnik.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Проверяет, может ли пользователь отправить запрос.
 * Реализация счётчика и Pro-статуса — в Фазе 5.
 */
class CheckSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    /** Реактивный поток статуса — UI подписывается и автоматически обновляется. */
    fun observe(): Flow<SubscriptionStatus> = repository.getStatus()

    /** Вызвать перед каждым отправленным сообщением. */
    suspend fun consumeRequest() = repository.incrementUsage()
}
