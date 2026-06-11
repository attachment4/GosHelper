"""Серверный учёт бесплатного лимита (анти-обход) по устройству."""
import time
from datetime import datetime, timezone

from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from ..db import get_session
from ..integrity import verify_integrity
from ..models import Subscription, UsageDaily

router = APIRouter(prefix="/usage", tags=["usage"])

FREE_DAILY_LIMIT = 3


def _today() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%d")


class UsageRequest(BaseModel):
    deviceId: str
    integrityToken: str | None = None


class UsageResponse(BaseModel):
    allowed: bool
    remaining: int
    isPro: bool


async def _is_pro(session: AsyncSession, device_id: str) -> bool:
    sub = await session.get(Subscription, device_id)
    return bool(sub and sub.expires_at > int(time.time() * 1000))


async def _usage(session: AsyncSession, device_id: str) -> UsageDaily:
    day = _today()
    key = f"{device_id}:{day}"
    row = await session.get(UsageDaily, key)
    if row is None:
        row = UsageDaily(id=key, device_id=device_id, day=day, count=0)
        session.add(row)
    return row


@router.post("/check", response_model=UsageResponse)
async def check(req: UsageRequest, session: AsyncSession = Depends(get_session)) -> UsageResponse:
    """Может ли устройство задать ещё вопрос (без списания)."""
    if await _is_pro(session, req.deviceId):
        return UsageResponse(allowed=True, remaining=-1, isPro=True)

    if not await verify_integrity(req.integrityToken):
        # подозрительное устройство — считаем, что лимит исчерпан
        return UsageResponse(allowed=False, remaining=0, isPro=False)

    row = await _usage(session, req.deviceId)
    remaining = max(0, FREE_DAILY_LIMIT - row.count)
    return UsageResponse(allowed=remaining > 0, remaining=remaining, isPro=False)


@router.post("/consume", response_model=UsageResponse)
async def consume(req: UsageRequest, session: AsyncSession = Depends(get_session)) -> UsageResponse:
    """Списать один вопрос. Вызывать после успешного ответа ИИ."""
    if await _is_pro(session, req.deviceId):
        return UsageResponse(allowed=True, remaining=-1, isPro=True)

    if not await verify_integrity(req.integrityToken):
        return UsageResponse(allowed=False, remaining=0, isPro=False)

    row = await _usage(session, req.deviceId)
    if row.count >= FREE_DAILY_LIMIT:
        await session.commit()
        return UsageResponse(allowed=False, remaining=0, isPro=False)

    row.count += 1
    await session.commit()
    remaining = max(0, FREE_DAILY_LIMIT - row.count)
    return UsageResponse(allowed=True, remaining=remaining, isPro=False)
