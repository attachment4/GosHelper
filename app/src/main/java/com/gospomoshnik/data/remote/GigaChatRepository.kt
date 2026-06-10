package com.gospomoshnik.data.remote

import com.gospomoshnik.data.prompt.PromptRepository
import com.gospomoshnik.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GigaChatRepository @Inject constructor(
    private val api: GigaChatApi,
    private val tokenManager: GigaChatTokenManager,
    private val prompts: PromptRepository
) {
    fun sendMessage(
        history: List<ChatMessage>,
        category: String
    ): Flow<String> = flow {
        val system = prompts.systemPrompt(category)
        val messages = buildList {
            add(GigaChatMessage("system", system))
            // Передаём последние сообщения для контекста (ограничиваем, чтобы
            // не раздувать запрос и укладываться в лимит токенов)
            history.takeLast(20).forEach { msg ->
                add(GigaChatMessage(msg.role, msg.content))
            }
        }

        val bearer   = tokenManager.getBearer()
        val response = api.chat(bearer = bearer, request = GigaChatRequest(messages = messages))
        emit(response.text)
    }
}
