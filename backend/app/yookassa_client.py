"""Минимальный клиент ЮKassa API (https://yookassa.ru/developers/api)."""
import uuid

import httpx

from .config import settings
from .plans import plan_price

API_BASE = "https://api.yookassa.ru/v3"


def _auth() -> tuple[str, str]:
    return (settings.yookassa_shop_id, settings.yookassa_secret_key)


async def create_payment(payment_token: str, plan: str, user_id: str) -> dict:
    """
    Создаёт платёж по токену, полученному в приложении через ЮKassa SDK.
    Секретный ключ используется ТОЛЬКО здесь, на сервере.
    """
    idempotence_key = str(uuid.uuid4())
    body = {
        "amount": {"value": plan_price(plan), "currency": "RUB"},
        "capture": True,
        "payment_token": payment_token,
        "description": f"ГосПомощник Pro — {plan}",
        "metadata": {"user_id": user_id, "plan": plan},
    }
    async with httpx.AsyncClient(timeout=30) as client:
        resp = await client.post(
            f"{API_BASE}/payments",
            json=body,
            auth=_auth(),
            headers={"Idempotence-Key": idempotence_key},
        )
        resp.raise_for_status()
        return resp.json()


async def get_payment(payment_id: str) -> dict:
    """Перезапрашивает статус платежа у ЮKassa — для надёжной верификации вебхука."""
    async with httpx.AsyncClient(timeout=30) as client:
        resp = await client.get(f"{API_BASE}/payments/{payment_id}", auth=_auth())
        resp.raise_for_status()
        return resp.json()
