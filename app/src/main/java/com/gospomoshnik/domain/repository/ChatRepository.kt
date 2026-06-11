package com.gospomoshnik.domain.repository

import com.gospomoshnik.domain.model.ChatMessage
import com.gospomoshnik.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория истории чатов.
 * Реализация — в data-слое через Room.
 */
interface ChatRepository {

    /** Все сессии, отсортированные по дате последнего обновления (новые вверху). */
    fun getAllSessions(): Flow<List<ChatSession>>

    /** Поиск сессий по заголовку. */
    fun searchSessions(query: String): Flow<List<ChatSession>>

    /** Сообщения конкретной сессии, отсортированные по времени. */
    fun getMessages(sessionId: Long): Flow<List<ChatMessage>>

    /** Создать новую сессию, вернуть её id. */
    suspend fun createSession(session: ChatSession): Long

    /** Обновить время изменения сессии (после каждого нового сообщения). */
    suspend fun touchSession(sessionId: Long)

    /** Сохранить одно сообщение. */
    suspend fun insertMessage(message: ChatMessage): Long

    /** Удалить одно сообщение по id (откат при ошибке отправки). */
    suspend fun deleteMessage(messageId: Long)

    /** Удалить сессию вместе со всеми её сообщениями. */
    suspend fun deleteSession(sessionId: Long)
}
