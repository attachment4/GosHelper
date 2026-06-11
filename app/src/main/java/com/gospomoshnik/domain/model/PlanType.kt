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
    val perMonthHint: String,
    val ctaText: String
) {
    MONTHLY (priceRub = 99,  days = 30,  title = "Месяц",  perMonthHint = "99 ₽/мес",  ctaText = "Оформить за 99 ₽/мес"),
    HALFYEAR(priceRub = 399, days = 182, title = "Полгода", perMonthHint = "67 ₽/мес",  ctaText = "Оформить за 399 ₽/полгода"),
    YEARLY  (priceRub = 799, days = 365, title = "Год",    perMonthHint = "67 ₽/мес",  ctaText = "Оформить за 799 ₽/год");

    val amountString: String get() = "%d.00".format(priceRub)
}
