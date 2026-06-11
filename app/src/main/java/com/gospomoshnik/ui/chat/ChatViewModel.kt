package com.gospomoshnik.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gospomoshnik.domain.model.ChatMessage
import com.gospomoshnik.domain.model.ChatSession
import com.gospomoshnik.domain.repository.ChatRepository
import com.gospomoshnik.domain.usecase.CheckSubscriptionUseCase
import com.gospomoshnik.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPaywall: Boolean = false,
    val sessionId: Long = 0L,
    val requestsLeft: Int = com.gospomoshnik.domain.model.FREE_DAILY_LIMIT,
    val isPro: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sendMessage: SendMessageUseCase,
    private val checkSubscription: CheckSubscriptionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val category: String = savedStateHandle["category"] ?: "general"

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // 0 — новая сессия; создаётся в БД лениво, при первом отправленном
    // сообщении, чтобы не плодить пустые "Новые диалоги"
    private var sessionId: Long = savedStateHandle["sessionId"] ?: 0L
    private var messagesJob: Job? = null

    init {
        if (sessionId > 0L) observeMessages()
        observeSubscription()
        // Готовый вопрос (переход из библиотеки документов) — подставляем в поле ввода
        val prefill: String = savedStateHandle["question"] ?: ""
        if (prefill.isNotBlank()) {
            _uiState.update { it.copy(inputText = prefill) }
        }
    }

    private fun observeMessages() {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages, sessionId = sessionId) }
            }
        }
    }

    private fun observeSubscription() {
        viewModelScope.launch {
            checkSubscription.observe().collect { sub ->
                _uiState.update {
                    it.copy(requestsLeft = sub.requestsLeft, isPro = sub.isPro)
                }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun send() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            val subscription = checkSubscription.get()
            if (!subscription.canSendMessage) {
                _uiState.update { it.copy(showPaywall = true) }
                return@launch
            }

            _uiState.update { it.copy(inputText = "", isLoading = true, error = null) }

            if (sessionId == 0L) {
                sessionId = chatRepository.createSession(
                    ChatSession(category = category, title = sessionTitle(text))
                )
                observeMessages()
            }

            val userMsg = ChatMessage(sessionId = sessionId, role = "user", content = text)
            val userMsgId = chatRepository.insertMessage(userMsg)

            val history = _uiState.value.messages + userMsg

            sendMessage(history, category)
                .catch { e ->
                    // Откатываем неотправленный вопрос: убираем его из истории и
                    // возвращаем текст в поле ввода, чтобы пользователь повторил
                    chatRepository.deleteMessage(userMsgId)
                    _uiState.update {
                        it.copy(isLoading = false, inputText = text, error = humanError(e))
                    }
                }
                .collect { reply ->
                    val assistantMsg = ChatMessage(
                        sessionId = sessionId,
                        role      = "assistant",
                        content   = reply
                    )
                    chatRepository.insertMessage(assistantMsg)
                    chatRepository.touchSession(sessionId)
                    // Списать запрос только после успешного ответа —
                    // ошибки сети не тратят лимит
                    checkSubscription.consumeRequest()
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun dismissPaywall() {
        _uiState.update { it.copy(showPaywall = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun sessionTitle(firstMessage: String) =
        firstMessage.take(60).let { if (firstMessage.length > 60) "$it…" else it }

    private fun humanError(e: Throwable): String = when {
        e is java.net.UnknownHostException     -> "Нет подключения к интернету"
        e is java.net.SocketTimeoutException   -> "Сервер не отвечает, попробуйте ещё раз"
        e is javax.net.ssl.SSLException        -> "Ошибка защищённого соединения"
        e.message?.contains("401") == true     -> "Ошибка авторизации GigaChat — проверьте ключ"
        else                                   -> e.message ?: "Неизвестная ошибка"
    }
}
