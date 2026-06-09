package com.gospomoshnik.data.remote

import com.gospomoshnik.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GigaChatRepository @Inject constructor(
    private val api: GigaChatApi,
    @Named("gigachat_token") private val authToken: String
) {
    fun sendMessage(
        history: List<ChatMessage>,
        category: String
    ): Flow<String> = flow {
        val messages = buildList {
            add(GigaChatMessage("system", systemPrompt(category)))
            history.forEach { msg ->
                add(GigaChatMessage(msg.role, msg.content))
            }
        }

        val response = api.chat(
            bearer  = "Bearer $authToken",
            request = GigaChatRequest(messages = messages)
        )
        emit(response.text)
    }

    private fun systemPrompt(category: String) = buildString {
        append("Ты — ИИ-консультант по законодательству Российской Федерации. ")
        append("Отвечай чётко, кратко и по существу. ")
        append("Всегда указывай конкретные статьи законов (КоАП РФ, ГК РФ, ТК РФ и т.д.). ")
        append("Говори на русском языке. ")
        when (category) {
            "gibdd"     -> append("Специализация: ГИБДД — штрафы, лишение прав, ДТП, обжалование постановлений.")
            "zhkh"      -> append("Специализация: ЖКХ — управляющие компании, квитанции, нормативы, жалобы в ГЖИ.")
            "labor"     -> append("Специализация: трудовые права — увольнение, невыплата зарплаты, дискриминация, ТК РФ.")
            "benefits"  -> append("Специализация: льготы и субсидии — пособия, компенсации, условия получения.")
            "court"     -> append("Специализация: судебные вопросы — подача исков, госпошлины, сроки, обжалование решений.")
            "documents" -> append("Специализация: юридические документы — жалобы, заявления, претензии, договоры.")
            else        -> append("Отвечай на вопросы по любой отрасли российского права.")
        }
    }
}
