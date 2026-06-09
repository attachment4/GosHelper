package com.gospomoshnik.domain.usecase

import com.gospomoshnik.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Отправляет сообщение в GigaChat и возвращает стримингованный ответ.
 * Полная реализация — Фаза 2 (подключение GigaChat API).
 *
 * @param history  История диалога (передаётся в API для поддержания контекста)
 * @param category Категория вопроса — формирует системный промпт
 * @return Flow<String> — токены ответа, приходящие по одному (SSE-стриминг)
 */
class SendMessageUseCase @Inject constructor(
    // GigaChatRepository будет добавлен в Фазе 2
) {
    operator fun invoke(
        history: List<ChatMessage>,
        category: String
    ): Flow<String> {
        // TODO Фаза 2: подключить GigaChatRepository
        throw NotImplementedError("Реализуется в Фазе 2")
    }
}
