"""ORM-модели: платежи и подписки."""
import time

from sqlalchemy import BigInteger, Integer, String
from sqlalchemy.orm import Mapped, mapped_column

from .db import Base


class Payment(Base):
    """Журнал платежей ЮKassa (идемпотентность и аудит)."""
    __tablename__ = "payments"

    id: Mapped[str] = mapped_column(String, primary_key=True)  # payment_id из ЮKassa
    user_id: Mapped[str] = mapped_column(String, index=True)
    plan: Mapped[str] = mapped_column(String)
    status: Mapped[str] = mapped_column(String)  # pending | succeeded | canceled
    amount: Mapped[str] = mapped_column(String)
    created_at: Mapped[int] = mapped_column(BigInteger, default=lambda: int(time.time() * 1000))


class UsageDaily(Base):
    """Счётчик бесплатных вопросов за день — серверный учёт против обхода."""
    __tablename__ = "usage_daily"

    # ключ: device_id + day ("2026-06-11")
    id: Mapped[str] = mapped_column(String, primary_key=True)  # f"{device_id}:{day}"
    device_id: Mapped[str] = mapped_column(String, index=True)
    day: Mapped[str] = mapped_column(String)
    count: Mapped[int] = mapped_column(Integer, default=0)


class Subscription(Base):
    """Текущая Pro-подписка пользователя."""
    __tablename__ = "subscriptions"

    user_id: Mapped[str] = mapped_column(String, primary_key=True)
    plan: Mapped[str] = mapped_column(String)
    expires_at: Mapped[int] = mapped_column(BigInteger)  # Unix-ms
    updated_at: Mapped[int] = mapped_column(BigInteger, default=lambda: int(time.time() * 1000))

    @property
    def is_active(self) -> bool:
        return self.expires_at > int(time.time() * 1000)
