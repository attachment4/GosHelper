package com.gospomoshnik.data.repository

import com.gospomoshnik.data.local.dao.ChatDao
import com.gospomoshnik.data.local.entity.toDomain
import com.gospomoshnik.data.local.entity.toEntity
import com.gospomoshnik.domain.model.ChatMessage
import com.gospomoshnik.domain.model.ChatSession
import com.gospomoshnik.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatDao
) : ChatRepository {

    override fun getAllSessions(): Flow<List<ChatSession>> =
        dao.getAllSessions().map { list -> list.map { it.toDomain() } }

    override fun searchSessions(query: String): Flow<List<ChatSession>> =
        dao.searchSessions(query).map { list -> list.map { it.toDomain() } }

    override fun getMessages(sessionId: Long): Flow<List<ChatMessage>> =
        dao.getMessages(sessionId).map { list -> list.map { it.toDomain() } }

    override suspend fun createSession(session: ChatSession): Long =
        dao.insertSession(session.toEntity())

    override suspend fun touchSession(sessionId: Long) =
        dao.touchSession(sessionId)

    override suspend fun insertMessage(message: ChatMessage): Long =
        dao.insertMessage(message.toEntity())

    override suspend fun deleteMessage(messageId: Long) =
        dao.deleteMessage(messageId)

    override suspend fun deleteSession(sessionId: Long) =
        dao.deleteSession(sessionId)

    override suspend fun deleteAllSessions() =
        dao.deleteAllSessions()
}
