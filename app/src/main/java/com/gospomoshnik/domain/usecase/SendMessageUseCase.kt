package com.gospomoshnik.domain.usecase

import com.gospomoshnik.data.remote.GigaChatRepository
import com.gospomoshnik.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val gigaChat: GigaChatRepository
) {
    operator fun invoke(
        history: List<ChatMessage>,
        category: String,
        reference: String? = null
    ): Flow<String> = gigaChat.sendMessage(history, category, reference)
}
