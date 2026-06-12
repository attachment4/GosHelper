"""Конфигурация из переменных окружения (.env)."""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # ── ЮKassa ────────────────────────────────────────────────────────────
    # Секретный ключ магазина — ТОЛЬКО здесь, на сервере. Никогда в APK.
    yookassa_shop_id: str = ""
    yookassa_secret_key: str = ""

    # ── GigaChat (ключ ТОЛЬКО на сервере, не в APK) ───────────────────────
    # base64(clientId:secret) из личного кабинета developers.sber.ru
    gigachat_auth: str = ""
    gigachat_scope: str = "GIGACHAT_API_PERS"   # PERS — физлицо, CORP/B2B — юрлицо
    # TLS до серверов Сбера: путь к russian_trusted_root_ca.pem или "false"
    gigachat_verify: str = "false"

    # ── База данных ───────────────────────────────────────────────────────
    database_url: str = "sqlite+aiosqlite:///./gospomoshnik.db"

    # ── Прочее ────────────────────────────────────────────────────────────
    return_url: str = "gospomoshnik://payment-result"  # deeplink назад в приложение
    environment: str = "production"  # "test" → песочница ЮKassa


settings = Settings()
