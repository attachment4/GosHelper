"""Pydantic-схемы запросов/ответов. Контракт с Android-приложением."""
from typing import Optional

from pydantic import BaseModel


class ConfirmRequest(BaseModel):
    paymentToken: str
    plan: str          # MONTHLY | HALFYEAR | YEARLY
    userId: str


class ConfirmResponse(BaseModel):
    status: str                      # succeeded | pending | canceled
    expiresAt: Optional[int] = None  # Unix-ms окончания Pro при успехе


class SubscriptionResponse(BaseModel):
    isPro: bool
    expiresAt: Optional[int] = None
