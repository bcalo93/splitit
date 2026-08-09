package com.splitit.presentation.settlement

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.model.Balance
import com.splitit.domain.model.Participant
import com.splitit.domain.model.Settlement
import com.splitit.domain.usecase.CalculateSessionBalancesUseCase
import com.splitit.domain.usecase.GenerateSettlementUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.value.SessionId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SettlementUiState(
    val participants: List<Participant> = emptyList(),
    val balances: List<Balance> = emptyList(),
    val settlement: Settlement? = null,
    val currentSourceRevision: Long = 0L,
    val isSettlementStale: Boolean = false,
    val canGenerateSettlement: Boolean = false,
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
)

class SettlementViewModel(
    private val sessionId: SessionId,
    private val observeSessionDetails: ObserveSessionDetailsUseCase,
    private val calculateSessionBalances: CalculateSessionBalancesUseCase,
    private val generateSettlement: GenerateSettlementUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SettlementUiState())
    val state: StateFlow<SettlementUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val snapshot = loadSnapshot()
                _state.update { it.apply(snapshot).copy(isLoading = false, isGenerating = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isGenerating = false,
                        errorMessage = throwable.message ?: "Could not load settlement.",
                    )
                }
            }
        }
    }

    fun generate() {
        val current = _state.value
        if (current.isLoading || current.isGenerating) return
        if (!current.canGenerateSettlement) {
            _state.update {
                it.copy(errorMessage = "Add at least two participants and an expense first.")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, errorMessage = null) }
            try {
                generateSettlement(sessionId)
                val snapshot = loadSnapshot()
                _state.update { it.apply(snapshot).copy(isGenerating = false, isLoading = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = throwable.message ?: "Could not generate settlement.",
                    )
                }
            }
        }
    }

    private suspend fun loadSnapshot(): SettlementSnapshot {
        val details = observeSessionDetails(sessionId)
        return SettlementSnapshot(
            participants = details.participants,
            balances = calculateSessionBalances(sessionId),
            settlement = details.latestSettlement,
            currentSourceRevision = details.currentSourceRevision,
            isSettlementStale = details.isSettlementStale,
            canGenerateSettlement = details.participants.size >= 2 && details.expenses.isNotEmpty(),
        )
    }

    private fun SettlementUiState.apply(snapshot: SettlementSnapshot): SettlementUiState {
        return copy(
            participants = snapshot.participants,
            balances = snapshot.balances,
            settlement = snapshot.settlement,
            currentSourceRevision = snapshot.currentSourceRevision,
            isSettlementStale = snapshot.isSettlementStale,
            canGenerateSettlement = snapshot.canGenerateSettlement,
            errorMessage = null,
        )
    }

    private data class SettlementSnapshot(
        val participants: List<Participant>,
        val balances: List<Balance>,
        val settlement: Settlement?,
        val currentSourceRevision: Long,
        val isSettlementStale: Boolean,
        val canGenerateSettlement: Boolean,
    )
}
