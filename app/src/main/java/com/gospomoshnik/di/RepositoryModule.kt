package com.gospomoshnik.di

import com.gospomoshnik.data.repository.ChatRepositoryImpl
import com.gospomoshnik.data.repository.SubscriptionRepositoryImpl
import com.gospomoshnik.domain.repository.ChatRepository
import com.gospomoshnik.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository
}
