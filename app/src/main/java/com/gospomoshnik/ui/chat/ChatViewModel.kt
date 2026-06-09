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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPaywall: Boolean = false,
    val sessionId: Long = 0L
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

    private var sessionId: Long = 0L

    init {
        initSession()
    }

    private fun initSession() {
        viewModelScope.launch {
            val session = ChatSession(
                category = category,
                title    = "Новый диалог"
            )
            sessionId = chatRepository.createSession(session)
            _uiState.update { it.copy(sessionId = sessionId) }

            chatRepository.getMessages(sessionId)
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages) }
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

            val userMsg = ChatMessage(sessionId = sessionId, role = "user", content = text)
            chatRepository.insertMessage(userMsg)
            updateSessionTitle(text)

            val history = _uiState.value.messages + userMsg

            sendMessage(history, category)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { reply ->
                    val assistantMsg = ChatMessage(
                        sessionId = sessionId,
                        role      = "assistant",
                        content   = reply
                    )
                    chatRepository.insertMessage(assistantMsg)
                    chatRepository.touchSession(sessionId)
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

    private suspend fun updateSessionTitle(firstMessage: String) {
        if (_uiState.value.messages.size > 1) return
        val title = firstMessage.take(60).let { if (firstMessage.length > 60) "$it…" else it }
        val updated = ChatSession(
            id       = sessionId,
            category = category,
            title    = title
        )
        chatRepository.createSession(updated)
    }
}
