package com.gospomoshnik.data.payment

import retrofit2.http.Body
import retrofit2.http.POST

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
