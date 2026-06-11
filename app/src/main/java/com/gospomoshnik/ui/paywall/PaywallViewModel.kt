package com.gospomoshnik.ui.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gospomoshnik.data.payment.PaymentOutcome
import com.gospomoshnik.data.payment.PaymentRepository
import com.gospomoshnik.domain.model.PlanType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PaymentState {
    data object Idle       : PaymentState
    data object Processing : PaymentState
    data object Success    : PaymentState
    data class  Error(val message: String) : PaymentState
}

data class PaywallUiState(
    val selectedPlan: PlanType = PlanType.YEARLY,
    val payment: PaymentState = PaymentState.Idle
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val payments: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    fun selectPlan(plan: PlanType) {
        _uiState.update { it.copy(selectedPlan = plan) }
    }

    /**
     * Запускает оплату. В реальном потоке здесь сначала вызывается ЮKassa SDK
     * (tokenization) для получения paymentToken, затем токен подтверждается на
     * бэкенде. Сейчас (без SDK/бэкенда) в DEBUG используется симуляция — см.
     * PaymentRepository. Передаём плейсхолдер токена.
     */
    fun pay() {
        val plan = _uiState.value.selectedPlan
        if (_uiState.value.payment == PaymentState.Processing) return

        viewModelScope.launch {
            _uiState.update { it.copy(payment = PaymentState.Processing) }
            // TODO: получить настоящий paymentToken через ЮKassa SDK tokenization
            val token = "SDK_TOKEN_PLACEHOLDER"
            val outcome = payments.confirmPayment(token, plan)
            _uiState.update {
                it.copy(
                    payment = when (outcome) {
                        is PaymentOutcome.Success -> PaymentState.Success
                        is PaymentOutcome.Pending -> PaymentState.Error("Платёж обрабатывается. Pro активируется после подтверждения.")
                        is PaymentOutcome.Failed  -> PaymentState.Error(outcome.reason)
                    }
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(payment = PaymentState.Idle) }
    }
}
