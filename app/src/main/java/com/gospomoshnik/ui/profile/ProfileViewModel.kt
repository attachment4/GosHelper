package com.gospomoshnik.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gospomoshnik.domain.model.SubscriptionStatus
import com.gospomoshnik.domain.repository.ChatRepository
import com.gospomoshnik.domain.usecase.CheckSubscriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    checkSubscription: CheckSubscriptionUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val subscription: StateFlow<SubscriptionStatus> = checkSubscription.observe()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubscriptionStatus()
        )

    /** Очистить всю историю чатов (необратимо). */
    fun clearAllHistory() = viewModelScope.launch { chatRepository.deleteAllSessions() }
}
