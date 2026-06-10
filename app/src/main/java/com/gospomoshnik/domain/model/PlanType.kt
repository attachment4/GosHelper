package com.gospomoshnik.domain.model

import androidx.compose.runtime.Immutable

/**
 * Тарифные планы Pro-подписки.
 * Цены в рублях; days — на сколько продлевается Pro после оплаты.
 */
@Immutable
enum class PlanType(
    val priceRub: Int,
    val days: Int,
    val title: String,
    val ctaText: String
) {
    MONTHLY(priceRub = 199, days = 30,  title = "Месяц", ctaText = "Оформить на месяц — 199 ₽"),
    YEARLY (priceRub = 990, days = 365, title = "Год",   ctaText = "Оформить на год — 990 ₽");

    val amountString: String get() = "%d.00".format(priceRub)
}
