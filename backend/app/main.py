"""Точка входа FastAPI-приложения."""
from contextlib import asynccontextmanager

from fastapi import FastAPI

from .db import init_db
from .routes import chat, health, payments, usage


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    yield


app = FastAPI(
    title="ГосПомощник — Payments API",
    version="1.0.0",
    lifespan=lifespan,
)

app.include_router(health.router)
app.include_router(payments.router, prefix="/api")
app.include_router(usage.router, prefix="/api")
app.include_router(chat.router, prefix="/api")


@app.get("/")
async def root() -> dict:
    return {"service": "gospomoshnik-backend", "docs": "/docs"}
