package com.gospomoshnik.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gospomoshnik.domain.model.ChatMessage

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity        = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns  = ["sessionId"],
            onDelete      = ForeignKey.CASCADE   // каскадное удаление при удалении сессии
        )
    ],
    indices = [Index("sessionId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String,           // "user" | "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun ChatMessageEntity.toDomain() = ChatMessage(
    id        = id,
    sessionId = sessionId,
    role      = role,
    content   = content,
    timestamp = timestamp
)

fun ChatMessage.toEntity() = ChatMessageEntity(
    id        = id,
    sessionId = sessionId,
    role      = role,
    content   = content,
    timestamp = timestamp
)
