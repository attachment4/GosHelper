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
        category: String,
        reference: String? = null
    ): Flow<String> = flow {
        val base = prompts.systemPrompt(category)
        // Если пользователь пришёл из конкретного документа — даём ИИ его текст
        // как ПРОВЕРЕННЫЙ источник и требуем опираться в первую очередь на него.
        val system = if (!reference.isNullOrBlank()) {
            base + "\n\n# Проверенный справочный материал по теме вопроса\n" +
                "Опирайся В ПЕРВУЮ ОЧЕРЕДЬ на этот выверенный материал, не противоречь ему. " +
                "Если в нём есть нужные статьи, сроки и нюансы — используй именно их.\n\n" +
                reference
        } else base

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
