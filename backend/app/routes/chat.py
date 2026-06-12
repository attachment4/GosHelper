"""
Прокси к GigaChat с серверным лимитом.

Закрывает сразу две проблемы:
  • ключ GigaChat не в APK (живёт на сервере);
  • лимит «3 вопроса в день» считается на сервере по device_id — не обходится
    очисткой данных приложения.

Приложение присылает готовый список сообщений (system-промпт с заземлением на
документы формируется на клиенте). Сервер только проверяет лимит и проксирует.
"""
import httpx
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from .. import gigachat
from ..db import get_session
from ..integrity import verify_integrity
from .usage import FREE_DAILY_LIMIT, _is_pro, _usage

router = APIRouter(prefix="/chat", tags=["chat"])


class Message(BaseModel):
    role: str
    content: str


class ChatRequest(BaseModel):
    deviceId: str
    messages: list[Message]
    integrityToken: str | None = None


class ChatResponse(BaseModel):
    reply: str
    remaining: int      # сколько бесплатных вопросов осталось (-1 = Pro/безлимит)
    isPro: bool


@router.post("", response_model=ChatResponse)
async def chat(req: ChatRequest, session: AsyncSession = Depends(get_session)) -> ChatResponse:
    is_pro = await _is_pro(session, req.deviceId)

    # Серверная проверка лимита (для бесплатных)
    if not is_pro:
        if not await verify_integrity(req.integrityToken):
            raise HTTPException(status_code=403, detail="Устройство не прошло проверку.")
        row = await _usage(session, req.deviceId)
        if row.count >= FREE_DAILY_LIMIT:
            raise HTTPException(status_code=402, detail="Дневной лимит исчерпан. Оформите Pro.")

    # Прокси к GigaChat (ключ на сервере)
    try:
        reply = await gigachat.chat([m.model_dump() for m in req.messages])
    except httpx.HTTPStatusError as e:
        raise HTTPException(status_code=502, detail=f"GigaChat: {e.response.status_code}") from e
    except Exception as e:  # noqa: BLE001
        raise HTTPException(status_code=502, detail=f"GigaChat недоступен: {e}") from e

    # Списываем лимит только после успешного ответа
    remaining = -1
    if not is_pro:
        row = await _usage(session, req.deviceId)
        row.count += 1
        await session.commit()
        remaining = max(0, FREE_DAILY_LIMIT - row.count)

    return ChatResponse(reply=reply, remaining=remaining, isPro=is_pro)
