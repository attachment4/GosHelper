# ГосПомощник — Backend (FastAPI)

Бэкенд для платежей ЮKassa. Секретный ключ магазина живёт **только здесь**, не в APK.

## Эндпоинты

| Метод | Путь | Назначение |
|-------|------|-----------|
| GET  | `/health` | Проверка живости |
| POST | `/api/payments/confirm` | Подтвердить токен → создать платёж в ЮKassa → активировать Pro |
| POST | `/api/payments/webhook` | Уведомления ЮKassa (статус платежа) |
| GET  | `/api/payments/subscription/{user_id}` | Текущий статус Pro |
| —    | `/docs` | Swagger UI |

### Контракт `/api/payments/confirm`
Запрос (совпадает с `PaymentApi.kt` в приложении):
```json
{ "paymentToken": "<токен из ЮKassa SDK>", "plan": "MONTHLY|HALFYEAR|YEARLY", "userId": "<id>" }
```
Ответ:
```json
{ "status": "succeeded|pending|canceled", "expiresAt": 1718000000000 }
```

## Тарифы (синхронизированы с PlanType.kt)
| Plan | Цена | Дней |
|------|------|------|
| MONTHLY | 99 ₽ | 30 |
| HALFYEAR | 399 ₽ | 182 |
| YEARLY | 799 ₽ | 365 |

## Локальный запуск
```bash
cd backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # заполнить ключами ЮKassa
uvicorn app.main:app --reload
# http://127.0.0.1:8000/docs
```

## Деплой на Amvera
1. Создать проект на https://amvera.io, тип — **Docker**.
2. Загрузить папку `backend/` (push в Amvera git или через веб-загрузку).
3. В настройках проекта задать переменные окружения из `.env.example`
   (`YOOKASSA_SHOP_ID`, `YOOKASSA_SECRET_KEY`, `DATABASE_URL`).
4. Подключить постоянный диск и указать `DATABASE_URL=sqlite+aiosqlite:////data/gospomoshnik.db`,
   чтобы база не терялась при перезапуске.
5. После деплоя прописать в приложении `PAYMENTS_BASE_URL=https://<ваш-домен>.amvera.io/api/`.
6. В ЛК ЮKassa указать URL вебхука: `https://<ваш-домен>.amvera.io/api/payments/webhook`
   (события `payment.succeeded`, `payment.canceled`).

## Безопасность
- `.env` не коммитится. Секретный ключ — только в переменных окружения сервера.
- Вебхук доверяет не телу запроса, а перезапросу статуса платежа у ЮKassa по `id`.
- TODO для усиления: проверка подписи/IP уведомлений ЮKassa, rate-limiting, стабильный `userId`.
