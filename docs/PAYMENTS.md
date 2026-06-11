# Платежи (ЮKassa)

## Архитектура (безопасная)

```
Приложение ──tokenize──> ЮKassa SDK ──> paymentToken
Приложение ──POST /payments/confirm {token, plan}──> НАШ бэкенд (FastAPI)
Бэкенд ──секретный ключ магазина──> ЮKassa API (Create Payment + 3DS)
Бэкенд ──webhook "succeeded"──> отмечает Pro, возвращает expiresAt
Приложение ──activatePro(expiresAt)──> Pro активен
```

**Главный принцип:** секретный ключ магазина живёт ТОЛЬКО на бэкенде.
В APK кладётся лишь публикуемый ключ (`YOOKASSA_KEY`) и `YOOKASSA_SHOP_ID` — ими нельзя списать деньги.

## Что уже реализовано в приложении
- `PlanType` — тарифы (99 ₽/мес, 399 ₽/полгода, 799 ₽/год) с длительностью.
- Бесплатно — 3 вопроса в день (`FREE_DAILY_LIMIT`).
- Бэкенд `backend/` (FastAPI) уже написан: `/api/payments/confirm`, `/webhook`,
  `/subscription/{userId}`. См. `backend/README.md`.
- `PaymentApi` + `PaymentRepository` — подтверждение токена на бэкенде и активация Pro.
- `PaywallViewModel` — состояния `Idle/Processing/Success/Error`.
- `PaywallScreen` — рабочий UI: выбор плана, спиннер при оплате, успех/ошибка, авто-закрытие.
- **DEBUG-симуляция** (`PAYMENTS_SIMULATE=true`): кнопка «Оформить» активирует Pro локально —
  чтобы прокликать весь сценарий на эмуляторе без бэкенда. В release симуляция выключена.

## Что нужно сделать для РЕАЛЬНОЙ оплаты

### 1. Реквизиты магазина
Зарегистрировать магазин в ЮKassa (нужен ИП/ООО), получить `shopId` и ключи.
В `local.properties`:
```
YOOKASSA_SHOP_ID=123456
YOOKASSA_KEY=live_xxxxxxxxxxxxxxxxxxxx    # публикуемый ключ
PAYMENTS_BASE_URL=https://ваш-бэкенд.amvera.io/api/
```

### 2. Подключить ЮKassa Android SDK
В `app/build.gradle.kts`:
```kotlin
implementation("ru.yoomoney.sdk.kassa.payments:yookassa-android-sdk:6.10.1")
```
> Версию проверить на https://github.com/yoomoney/yookassa-android-sdk —
> SDK тянет транзитивные зависимости, после добавления обязательно Gradle Sync и сборка.

Затем реализовать tokenization в `PaywallViewModel.pay()`:
- собрать `PaymentParameters(amount, shopId=YOOKASSA_SHOP_ID, clientApplicationKey=YOOKASSA_KEY, ...)`
- запустить `Checkout.createTokenizeIntent(...)` через `rememberLauncherForActivityResult`
- из результата получить `paymentToken` и передать в `payments.confirmPayment(token, plan)`
  (сейчас туда уходит плейсхолдер `SDK_TOKEN_PLACEHOLDER`).

### 3. Бэкенд (FastAPI на Amvera)
Эндпоинт `POST /payments/confirm`:
- принимает `{ paymentToken, plan, userId }`
- создаёт платёж в ЮKassa секретным ключом (`Idempotence-Key`!)
- проводит подтверждение, по вебхуку ловит статус `succeeded`
- возвращает `{ status, expiresAt }`

## Чек-лист релиза платежей
- [ ] Магазин ЮKassa подтверждён, ключи в local.properties (не в git)
- [ ] SDK подключён, tokenization реализована
- [ ] Бэкенд /payments/confirm работает и проверен на боевом ключе
- [ ] Проверена отмена/возврат, обработка `pending`/`canceled`
- [ ] В release `PAYMENTS_SIMULATE=false` (уже так)
