package com.gospomoshnik.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gospomoshnik.data.local.dao.ChatDao
import com.gospomoshnik.data.local.entity.ChatMessageEntity
import com.gospomoshnik.data.local.entity.ChatSessionEntity

@Database(
    entities  = [ChatSessionEntity::class, ChatMessageEntity::class],
    version   = 1,
    exportSchema = true   // схема сохраняется в app/schemas/ для будущих миграций
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
