Сюда нужно положить файл языка для OCR (распознавание текста с фото):

  rus.traineddata

Скачать (~15 МБ) из официального репозитория Tesseract:
  https://github.com/tesseract-ocr/tessdata_best/raw/main/rus.traineddata
  (или tessdata_fast/rus.traineddata — меньше и быстрее, чуть ниже качество)

Положить файл рядом с этим README:
  app/src/main/assets/tessdata/rus.traineddata

Без этого файла прикрепление фото покажет «Не добавлен языковой файл»,
но приложение работать продолжит.

Опционально для распознавания латиницы/цифр добавьте также eng.traineddata.
