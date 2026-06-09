package com.gospomoshnik.domain.usecase

import com.gospomoshnik.domain.model.ChatSession
import com.gospomoshnik.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Возвращает список всех сессий или результат поиска.
 */
class GetChatHistoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    /** Все сессии (для экрана «Чаты» и блока «Недавние» на главном меню). */
    fun getAllSessions(): Flow<List<ChatSession>> = repository.getAllSessions()

    /** Поиск по заголовку сессии. */
    fun search(query: String): Flow<List<ChatSession>> =
        if (query.isBlank()) repository.getAllSessions()
        else repository.searchSessions(query)
}
