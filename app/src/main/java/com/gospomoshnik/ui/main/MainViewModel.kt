package com.gospomoshnik.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gospomoshnik.data.payment.PaymentRepository
import com.gospomoshnik.domain.model.ChatSession
import com.gospomoshnik.domain.model.FREE_DAILY_LIMIT
import com.gospomoshnik.domain.repository.ChatRepository
import com.gospomoshnik.domain.usecase.CheckSubscriptionUseCase
import com.gospomoshnik.domain.usecase.GetChatHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val recentSessions: List<ChatSession> = emptyList(),
    val requestsLeft: Int = FREE_DAILY_LIMIT,
    val isPro: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getChatHistory: GetChatHistoryUseCase,
    private val checkSubscription: CheckSubscriptionUseCase,
    private val payments: PaymentRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    init {
        // Сверяем Pro с бэкендом (восстановление после переустановки, истечение)
        viewModelScope.launch { payments.syncSubscription() }
    }

    /** Очистить всю историю чатов. «Недавние» обновятся сами (реактивный поток). */
    fun clearAllHistory() = viewModelScope.launch { chatRepository.deleteAllSessions() }

    val uiState: StateFlow<MainUiState> = combine(
        getChatHistory.getAllSessions(),
        checkSubscription.observe()
    ) { sessions, subscription ->
        MainUiState(
            recentSessions = sessions,
            requestsLeft   = subscription.requestsLeft,
            isPro          = subscription.isPro
        )
    }.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = MainUiState()
    )
}
