package com.gospomoshnik.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gospomoshnik.domain.model.ChatSession

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Маппинг Entity ↔ Domain
fun ChatSessionEntity.toDomain() = ChatSession(
    id        = id,
    category  = category,
    title     = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ChatSession.toEntity() = ChatSessionEntity(
    id        = id,
    category  = category,
    title     = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)
