"""Каталог тарифов. Должен совпадать с PlanType.kt в Android-приложении."""

PLANS = {
    "MONTHLY":  {"price": "99.00",  "days": 30,  "title": "Месяц"},
    "HALFYEAR": {"price": "399.00", "days": 182, "title": "Полгода"},
    "YEARLY":   {"price": "799.00", "days": 365, "title": "Год"},
}


def is_valid_plan(plan: str) -> bool:
    return plan in PLANS


def plan_days(plan: str) -> int:
    return PLANS[plan]["days"]


def plan_price(plan: str) -> str:
    return PLANS[plan]["price"]
