package com.gospomoshnik.domain.usecase

import com.gospomoshnik.domain.model.ChatMessage
import javax.inject.Inject

/**
 * Генерирует документ на основе диалога.
 * Полная реализация — Фаза 4 (iTextG + шаблоны).
 *
 * @param history   История диалога, из которой ИИ извлечёт данные для заполнения
 * @param template  Идентификатор шаблона: "gibdd_complaint", "zhkh_claim" и т.д.
 * @return          Путь к сгенерированному PDF-файлу
 */
class GenerateDocumentUseCase @Inject constructor() {
    suspend operator fun invoke(
        history: List<ChatMessage>,
        template: String
    ): String {
        // TODO Фаза 4: GigaChat-промпт → заполнение полей → iTextG PDF
        throw NotImplementedError("Реализуется в Фазе 4")
    }
}
