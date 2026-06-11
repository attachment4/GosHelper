package com.gospomoshnik.data.payment

import com.gospomoshnik.BuildConfig
import com.gospomoshnik.data.device.DeviceIdProvider
import com.gospomoshnik.domain.model.PlanType
import com.gospomoshnik.domain.repository.SubscriptionRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

sealed interface PaymentOutcome {
    data class Success(val expiresAt: Long) : PaymentOutcome
    data class Failed(val reason: String)   : PaymentOutcome
    data object Pending                      : PaymentOutcome
}

/**
 * Подтверждает оплату на бэкенде и активирует Pro.
 *
 * В DEBUG (нет бэкенда) — симулирует успешный платёж, чтобы можно было
 * прокликать весь сценарий на эмуляторе. В release реально ходит на бэкенд;
 * пока бэкенд не поднят, вернёт ошибку — никакого «фейкового Pro» в проде.
 */
@Singleton
class PaymentRepository @Inject constructor(
    private val api: PaymentApi,
    private val subscriptions: SubscriptionRepository,
    private val deviceId: DeviceIdProvider
) {
    suspend fun confirmPayment(paymentToken: String, plan: PlanType): PaymentOutcome {
        if (BuildConfig.DEBUG && BuildConfig.PAYMENTS_SIMULATE) {
            delay(1200)   // имитация сетевой задержки/3DS
            val expiresAt = System.currentTimeMillis() + plan.days * 24L * 60 * 60 * 1000
            subscriptions.activatePro(expiresAt)
            return PaymentOutcome.Success(expiresAt)
        }

        return runCatching {
            val resp = api.confirm(
                ConfirmRequest(
                    paymentToken = paymentToken,
                    plan         = plan.name,
                    userId       = deviceId.get()
                )
            )
            when {
                resp.status == "succeeded" && resp.expiresAt != null -> {
                    subscriptions.activatePro(resp.expiresAt)
                    PaymentOutcome.Success(resp.expiresAt)
                }
                resp.status == "pending" -> PaymentOutcome.Pending
                else -> PaymentOutcome.Failed("Платёж не подтверждён (${resp.status})")
            }
        }.getOrElse { e ->
            PaymentOutcome.Failed(e.message ?: "Сервис оплаты недоступен")
        }
    }

    /**
     * Синхронизирует статус Pro с бэкендом (например, при старте приложения
     * или после переустановки). В DEBUG-симуляции пропускается.
     */
    suspend fun syncSubscription() {
        if (BuildConfig.DEBUG && BuildConfig.PAYMENTS_SIMULATE) return
        runCatching {
            val resp = api.subscription(deviceId.get())
            if (resp.isPro && resp.expiresAt != null) {
                subscriptions.activatePro(resp.expiresAt)
            } else {
                subscriptions.deactivatePro()
            }
        }
    }
}
