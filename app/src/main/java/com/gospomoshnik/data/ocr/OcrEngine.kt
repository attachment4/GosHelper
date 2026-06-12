package com.gospomoshnik.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed interface OcrResult {
    data class Success(val text: String) : OcrResult
    data class Failure(val reason: String) : OcrResult
}

/**
 * Офлайн-распознавание текста с фото (Tesseract, кириллица).
 *
 * Требует языковой файл: assets/tessdata/rus.traineddata
 * (скачать из https://github.com/tesseract-ocr/tessdata_best, ~15 МБ).
 * Без файла вернёт понятную ошибку, приложение не падает.
 */
@Singleton
class OcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tessRoot = File(context.filesDir, "tess")          // содержит tessdata/
    private val traineddata = File(tessRoot, "tessdata/rus.traineddata")

    suspend fun recognize(uri: Uri): OcrResult = withContext(Dispatchers.IO) {
        if (!ensureLanguageData()) {
            return@withContext OcrResult.Failure(
                "Не добавлен языковой файл распознавания (rus.traineddata)."
            )
        }
        val bitmap = decodeBitmap(uri)
            ?: return@withContext OcrResult.Failure("Не удалось открыть изображение.")

        val tess = TessBaseAPI()
        try {
            if (!tess.init(tessRoot.absolutePath, "rus")) {
                return@withContext OcrResult.Failure("Не удалось инициализировать распознавание.")
            }
            tess.setImage(bitmap)
            val text = tess.getUTF8Text()?.trim().orEmpty()
            if (text.isBlank()) OcrResult.Failure("Текст на изображении не распознан.")
            else OcrResult.Success(text)
        } catch (e: Exception) {
            OcrResult.Failure(e.message ?: "Ошибка распознавания.")
        } finally {
            runCatching { tess.recycle() }
            runCatching { bitmap.recycle() }
        }
    }

    /** Копирует rus.traineddata из assets в filesDir/tess/tessdata при первом запуске. */
    private fun ensureLanguageData(): Boolean {
        if (traineddata.exists()) return true
        return runCatching {
            traineddata.parentFile?.mkdirs()
            context.assets.open("tessdata/rus.traineddata").use { input ->
                traineddata.outputStream().use { output -> input.copyTo(output) }
            }
            traineddata.exists()
        }.getOrDefault(false)
    }

    private fun decodeBitmap(uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri).use { stream ->
            val opts = BitmapFactory.Options().apply { inSampleSize = 1 }
            BitmapFactory.decodeStream(stream, null, opts)
        }
    }.getOrNull()
}
