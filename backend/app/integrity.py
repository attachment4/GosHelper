"""
Проверка Play Integrity токена.

Полная проверка требует Google Play Integrity API (расшифровка/верификация
токена через сервис Google по ключам проекта Play Console). Здесь — заготовка:
в окружении 'test' пропускаем, в 'production' — место для реальной проверки.
"""
from .config import settings


async def verify_integrity(token: str | None) -> bool:
    """
    Возвращает True, если устройство/приложение признаны подлинными.

    TODO production: вызвать Play Integrity API:
      - расшифровать integrity verdict;
      - проверить appRecognitionVerdict == PLAY_RECOGNIZED;
      - проверить deviceRecognitionVerdict (MEETS_DEVICE_INTEGRITY);
      - сверить package name и nonce.
    """
    if settings.environment != "production":
        return True
    # Пока боевая проверка не настроена — не блокируем, но логируем отсутствие токена.
    return token is not None and len(token) > 0
