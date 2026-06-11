"""Платёжные эндпоинты: подтверждение токена, вебхук ЮKassa, статус подписки."""
import time

import httpx
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from ..db import get_session
from ..models import Payment, Subscription
from ..plans import is_valid_plan, plan_days
from ..schemas import ConfirmRequest, ConfirmResponse, SubscriptionResponse
from ..yookassa_client import create_payment, get_payment

router = APIRouter(prefix="/payments", tags=["payments"])


async def _activate(session: AsyncSession, user_id: str, plan: str) -> int:
    """Продлевает Pro: от max(сейчас, текущая дата окончания) + дни тарифа."""
    now_ms = int(time.time() * 1000)
    sub = await session.get(Subscription, user_id)
    base = max(now_ms, sub.expires_at) if sub and sub.expires_at > now_ms else now_ms
    expires_at = base + plan_days(plan) * 24 * 60 * 60 * 1000

    if sub:
        sub.plan = plan
        sub.expires_at = expires_at
        sub.updated_at = now_ms
    else:
        session.add(Subscription(user_id=user_id, plan=plan, expires_at=expires_at, updated_at=now_ms))
    await session.commit()
    return expires_at


@router.post("/confirm", response_model=ConfirmResponse)
async def confirm(req: ConfirmRequest, session: AsyncSession = Depends(get_session)) -> ConfirmResponse:
    if not is_valid_plan(req.plan):
        raise HTTPException(status_code=400, detail="Неизвестный тариф")

    try:
        payment = await create_payment(req.paymentToken, req.plan, req.userId)
    except httpx.HTTPStatusError as e:
        raise HTTPException(status_code=502, detail=f"ЮKassa: {e.response.text}") from e

    payment_id = payment["id"]
    status = payment["status"]

    # Журналируем платёж
    session.add(Payment(
        id=payment_id, user_id=req.userId, plan=req.plan,
        status=status, amount=payment["amount"]["value"],
    ))
    await session.commit()

    if status == "succeeded":
        expires_at = await _activate(session, req.userId, req.plan)
        return ConfirmResponse(status="succeeded", expiresAt=expires_at)

    # pending — финальный статус придёт вебхуком; приложение покажет «обрабатывается»
    return ConfirmResponse(status=status, expiresAt=None)


@router.post("/webhook")
async def webhook(payload: dict, session: AsyncSession = Depends(get_session)) -> dict:
    """
    Уведомление ЮKassa. Доверяем НЕ телу запроса, а перезапросу статуса по API:
    берём id из уведомления и повторно читаем платёж у ЮKassa.
    """
    obj = payload.get("object", {})
    payment_id = obj.get("id")
    if not payment_id:
        raise HTTPException(status_code=400, detail="Нет id платежа")

    try:
        payment = await get_payment(payment_id)
    except httpx.HTTPStatusError as e:
        raise HTTPException(status_code=502, detail="ЮKassa недоступна") from e

    status = payment["status"]
    meta = payment.get("metadata", {})
    user_id = meta.get("user_id")
    plan = meta.get("plan")

    # Обновляем журнал
    rec = await session.get(Payment, payment_id)
    if rec:
        rec.status = status
        await session.commit()

    if status == "succeeded" and user_id and is_valid_plan(plan):
        await _activate(session, user_id, plan)

    return {"ok": True}


@router.get("/subscription/{user_id}", response_model=SubscriptionResponse)
async def subscription(user_id: str, session: AsyncSession = Depends(get_session)) -> SubscriptionResponse:
    sub = await session.get(Subscription, user_id)
    if sub and sub.is_active:
        return SubscriptionResponse(isPro=True, expiresAt=sub.expires_at)
    return SubscriptionResponse(isPro=False, expiresAt=None)
