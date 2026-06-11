"""Конфигурация из переменных окружения (.env)."""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # ── ЮKassa ────────────────────────────────────────────────────────────
    # Секретный ключ магазина — ТОЛЬКО здесь, на сервере. Никогда в APK.
    yookassa_shop_id: str = ""
    yookassa_secret_key: str = ""

    # ── База данных ───────────────────────────────────────────────────────
    database_url: str = "sqlite+aiosqlite:///./gospomoshnik.db"

    # ── Прочее ────────────────────────────────────────────────────────────
    return_url: str = "gospomoshnik://payment-result"  # deeplink назад в приложение
    environment: str = "production"  # "test" → песочница ЮKassa


settings = Settings()
