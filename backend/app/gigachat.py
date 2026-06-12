"""
Клиент GigaChat на стороне сервера.

Ключ (base64 clientId:secret) живёт здесь, в окружении сервера, и НЕ попадает
в APK. Приложение шлёт только список сообщений; токен и секрет — на сервере.
"""
import time
import uuid

import httpx

from .config import settings

OAUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
CHAT_URL = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"


def _verify():
    """TLS-проверка до Сбера: путь к CA-бандлу или False (отключить)."""
    v = settings.gigachat_verify
    if v.lower() in ("false", "0", "no", ""):
        return False
    return v


class _TokenCache:
    access_token: str = ""
    expires_at: float = 0.0  # секунды epoch


_cache = _TokenCache()


async def _bearer() -> str:
    now = time.time()
    if _cache.access_token and now < _cache.expires_at - 60:
        return _cache.access_token

    async with httpx.AsyncClient(timeout=30, verify=_verify()) as client:
        resp = await client.post(
            OAUTH_URL,
            headers={
                "Authorization": f"Basic {settings.gigachat_auth}",
                "RqUID": str(uuid.uuid4()),
                "Content-Type": "application/x-www-form-urlencoded",
            },
            data={"scope": settings.gigachat_scope},
        )
        resp.raise_for_status()
        data = resp.json()
        _cache.access_token = data["access_token"]
        # expires_at в ответе — миллисекунды
        _cache.expires_at = data.get("expires_at", 0) / 1000 or (now + 1500)
        return _cache.access_token


async def chat(messages: list[dict], temperature: float = 0.3, max_tokens: int = 2400) -> str:
    """Отправляет сообщения в GigaChat и возвращает текст ответа."""
    token = await _bearer()
    body = {
        "model": "GigaChat",
        "messages": messages,
        "temperature": temperature,
        "max_tokens": max_tokens,
        "stream": False,
    }
    async with httpx.AsyncClient(timeout=90, verify=_verify()) as client:
        resp = await client.post(
            CHAT_URL,
            headers={"Authorization": f"Bearer {token}"},
            json=body,
        )
        resp.raise_for_status()
        data = resp.json()
        return data["choices"][0]["message"]["content"]
