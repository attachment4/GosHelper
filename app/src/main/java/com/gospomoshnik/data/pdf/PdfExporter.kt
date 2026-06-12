package com.gospomoshnik.data.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Генерация PDF из текста документа на встроенном Android PdfDocument
 * (без внешних библиотек и без AGPL-лицензий iText). Поддерживает кириллицу
 * (системный шрифт), перенос строк и многостраничность.
 */
@Singleton
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun export(title: String, content: String): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val pageW = 595; val pageH = 842   // A4 @ 72 dpi
            val margin = 48f
            val usable = pageW - 2 * margin

            val bodyPaint  = Paint().apply { textSize = 12f; color = Color.BLACK; isAntiAlias = true }
            val titlePaint = Paint().apply { textSize = 17f; color = Color.BLACK; isFakeBoldText = true; isAntiAlias = true }

            val pdf = PdfDocument()
            var pageNum = 1
            var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create())
            var canvas = page.canvas
            var y = margin + 18f

            fun newPage() {
                pdf.finishPage(page)
                pageNum++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create())
                canvas = page.canvas
                y = margin + 18f
            }

            // Заголовок
            for (line in wrap(title, titlePaint, usable)) {
                if (y > pageH - margin) newPage()
                canvas.drawText(line, margin, y, titlePaint); y += 24f
            }
            y += 12f

            // Тело (с сохранением пустых строк как отступов)
            for (paragraph in content.split("\n")) {
                if (paragraph.isBlank()) { y += 14f; continue }
                for (line in wrap(paragraph, bodyPaint, usable)) {
                    if (y > pageH - margin) newPage()
                    canvas.drawText(line, margin, y, bodyPaint); y += 17f
                }
            }
            pdf.finishPage(page)

            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val file = File(dir, "Документ_${System.currentTimeMillis()}.pdf")
            file.outputStream().use { pdf.writeTo(it) }
            pdf.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull()
    }

    /** Перенос строки по ширине страницы. */
    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        val out = ArrayList<String>()
        var cur = StringBuilder()
        for (word in text.split(" ")) {
            val test = if (cur.isEmpty()) word else "$cur $word"
            if (paint.measureText(test) <= maxWidth) {
                cur = StringBuilder(test)
            } else {
                if (cur.isNotEmpty()) out.add(cur.toString())
                cur = StringBuilder(word)
            }
        }
        if (cur.isNotEmpty()) out.add(cur.toString())
        return if (out.isEmpty()) listOf("") else out
    }
}
