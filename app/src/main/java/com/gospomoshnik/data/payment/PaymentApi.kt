package com.gospomoshnik.data.payment

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API нашего бэкенда (FastAPI на Amvera), а НЕ ЮKassa напрямую.
 *
 * Поток оплаты (безопасный):
 *   1. Приложение через ЮKassa SDK получает paymentToken (без секретного ключа).
 *   2. Отправляет токен сюда → POST /payments/confirm.
 *   3. Бэкенд секретным ключом магазина создаёт платёж в ЮKassa, проводит 3DS,
 *      по вебхуку получает статус "succeeded" и возвращает дату окончания Pro.
 *   4. Приложение активирует Pro по доверенному ответу бэкенда.
 *
 * Секретный ключ магазина НИКОГДА не попадает в APK — иначе им можно списывать
 * деньги от имени магазина.
 */
interface PaymentApi {

    @POST("payments/confirm")
    suspend fun confirm(@Body request: ConfirmRequest): ConfirmResponse

    @GET("payments/subscription/{userId}")
    suspend fun subscription(@Path("userId") userId: String): SubscriptionResponse
}

data class ConfirmRequest(
    val paymentToken: String,
    val plan: String,          // "MONTHLY" | "YEARLY"
    val userId: String         // анонимный идентификатор устройства/аккаунта
)

data class ConfirmResponse(
    val status: String,        // "succeeded" | "pending" | "canceled"
    val expiresAt: Long?       // Unix-ms окончания Pro при успехе
)

data class SubscriptionResponse(
    val isPro: Boolean,
    val expiresAt: Long?
)
