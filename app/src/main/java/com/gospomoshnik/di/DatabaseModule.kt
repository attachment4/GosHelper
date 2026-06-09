package com.gospomoshnik.di

import android.content.Context
import androidx.room.Room
import com.gospomoshnik.data.local.AppDatabase
import com.gospomoshnik.data.local.dao.ChatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gospomoshnik.db"
        )
        .fallbackToDestructiveMigration()   // в debug; для prod — явные Migration
        .build()

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()
}
