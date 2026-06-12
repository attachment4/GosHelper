Сюда нужно положить языковой файл OCR (распознавание текста с фото):

  rus.traineddata   (~15 МБ)

Скачать из официального репозитория Tesseract:
  https://github.com/tesseract-ocr/tessdata_best/raw/main/rus.traineddata
  (или tessdata_fast/rus.traineddata — меньше и быстрее)

Положить рядом с этим README:
  app/src/main/assets/tessdata/rus.traineddata

Без файла кнопка-скрепка работает, но скажет «не добавлен языковой файл».
Зависимость: com.github.adaptech-cz:Tesseract4Android:4.8.0 (репозиторий JitPack).
