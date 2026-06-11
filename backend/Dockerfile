FROM python:3.12-slim

WORKDIR /app

# Зависимости — отдельным слоем для кэширования
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app

EXPOSE 8000

# Amvera пробрасывает порт через переменную PORT; по умолчанию 8000
CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port ${PORT:-8000}"]
