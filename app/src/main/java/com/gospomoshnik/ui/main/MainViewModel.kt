package com.gospomoshnik.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gospomoshnik.domain.model.ChatSession
import com.gospomoshnik.domain.usecase.CheckSubscriptionUseCase
import com.gospomoshnik.domain.usecase.GetChatHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MainUiState(
    val recentSessions: List<ChatSession> = emptyList(),
    val requestsLeft: Int = 10,
    val isLoading: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getChatHistory: GetChatHistoryUseCase,
    private val checkSubscription: CheckSubscriptionUseCase
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        getChatHistory.getAllSessions(),
        checkSubscription.observe()
    ) { sessions, subscription ->
        MainUiState(
            recentSessions = sessions,
            requestsLeft   = subscription.requestsLeft
        )
    }.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = MainUiState()
    )
}
