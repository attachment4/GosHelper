package com.gospomoshnik.data.prompt

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Системные промпты по отделам хранятся в assets/prompts (файлы .md)
 * и собираются как: общая основа (_base) + профильный файл категории.
 * Это позволяет редактировать «характер» консультанта без пересборки логики.
 */
@Singleton
class PromptRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cache = mutableMapOf<String, String>()

    /** Готовый системный промпт для категории с подставленной текущей датой. */
    suspend fun systemPrompt(category: String): String = withContext(Dispatchers.IO) {
        val base    = load("_base")
        val profile = load(category.ifBlank { "general" })
        val today   = SimpleDateFormat("d MMMM yyyy 'г.'", Locale("ru")).format(Date())
        "$base\n\n$profile".replace("{DATE}", today)
    }

    private fun load(name: String): String = cache.getOrPut(name) {
        runCatching {
            context.assets.open("prompts/$name.md").bufferedReader().use { it.readText() }
        }.getOrElse {
            // На случай отсутствующего файла — не валим работу чата
            if (name == "_base") "Ты — ИИ-консультант по законодательству РФ. Отвечай на русском, со ссылками на статьи законов."
            else ""
        }
    }
}
