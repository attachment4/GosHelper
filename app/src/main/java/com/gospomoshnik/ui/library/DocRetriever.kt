package com.gospomoshnik.ui.library

/**
 * Лёгкий поиск по библиотеке прямо на устройстве (без бэкенда).
 * По тексту вопроса подбирает наиболее релевантные документы, чтобы
 * подмешать их ИИ как проверенный источник (заземление ответов).
 *
 * Ищем по метаданным каталога (заголовок, подзаголовок, готовый вопрос,
 * подкатегория, название отдела) — этого достаточно для хорошего попадания,
 * а индекс не требует чтения 138 файлов на каждый запрос.
 */
object DocRetriever {

    private val stop = setOf(
        "как", "что", "где", "когда", "почему", "можно", "нужно", "если", "или",
        "это", "для", "при", "после", "меня", "мне", "мой", "моя", "вопрос",
        "подскажите", "пожалуйста", "хочу", "надо", "буду", "есть"
    )

    private fun tokens(text: String): List<String> =
        text.lowercase()
            .map { if (it.isLetterOrDigit()) it else ' ' }
            .joinToString("")
            .split(' ')
            .filter { it.length >= 4 && it !in stop }

    /** Возвращает до [limit] наиболее релевантных документов (или пусто). */
    fun search(query: String, category: String?, limit: Int = 2): List<LibraryDoc> {
        val q = tokens(query)
        if (q.isEmpty()) return emptyList()

        data class Scored(val doc: LibraryDoc, val score: Int)

        val results = docsCatalog.flatMap { cat ->
            cat.docs.map { doc ->
                val hay = tokens("${doc.title} ${doc.subtitle} ${doc.question} ${doc.subcategory} ${cat.title}")
                var score = 0
                for (t in q) {
                    if (hay.any { it == t || it.startsWith(t) || t.startsWith(it) }) score += 2
                }
                if (category != null && cat.key == category) score += 1   // лёгкий буст профильного отдела
                Scored(doc, score)
            }
        }.filter { it.score >= 2 }
            .sortedByDescending { it.score }

        return results.take(limit).map { it.doc }
    }
}
