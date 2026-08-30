package com.splitit.presentation.settlement

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.model.Balance
import com.splitit.domain.model.Debt
import com.splitit.domain.model.Participant
import com.splitit.domain.model.Settlement
import com.splitit.domain.model.SettlementTransfer
import com.splitit.domain.usecase.CalculateGroupBalancesUseCase
import com.splitit.domain.usecase.GenerateSettlementUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.RecordTransferPaymentUseCase
import com.splitit.domain.value.GroupId
import com.splitit.domain.value.TransferId
import com.splitit.localization.LocalizedString
import com.splitit.localization.LocalizationService
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
    val recordingTransferId: TransferId? = null,
    val errorMessage: String? = null,
)

class SettlementViewModel(
    private val groupId: GroupId,
    private val observeGroupDetails: ObserveGroupDetailsUseCase,
    private val calculateGroupBalances: CalculateGroupBalancesUseCase,
    private val generateSettlement: GenerateSettlementUseCase,
    private val recordTransferPaymentUseCase: RecordTransferPaymentUseCase,
    private val localization: LocalizationService,
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
                        errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotLoadSettlement),
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
                it.copy(errorMessage = localization.getString(LocalizedString.ErrorAddParticipantsAndExpense))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, errorMessage = null) }
            try {
                generateSettlement(groupId)
                val snapshot = loadSnapshot()
                _state.update { it.apply(snapshot).copy(isGenerating = false, isLoading = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotGenerateSettlement),
                    )
                }
            }
        }
    }

    fun recordTransferPayment(transfer: SettlementTransfer) {
        val current = _state.value
        if (current.isLoading || current.isGenerating || current.recordingTransferId != null) return

        viewModelScope.launch {
            _state.update { it.copy(recordingTransferId = transfer.id, errorMessage = null) }
            try {
                recordTransferPaymentUseCase(
                    groupId = groupId,
                    debt = Debt(
                        fromParticipantId = transfer.fromParticipantId,
                        toParticipantId = transfer.toParticipantId,
                        amount = transfer.amount,
                    ),
                )
                val snapshot = loadSnapshot()
                _state.update {
                    it.apply(snapshot).copy(
                        recordingTransferId = null,
                        isLoading = false,
                        isGenerating = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        recordingTransferId = null,
                        errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotRecordPayment),
                    )
                }
            }
        }
    }

    private suspend fun loadSnapshot(): SettlementSnapshot {
        var details = observeGroupDetails(groupId)
        val canGenerateSettlement = details.participants.size >= 2 && details.expenses.isNotEmpty()
        val shouldGenerateSettlement = canGenerateSettlement &&
            (details.latestSettlement == null || details.isSettlementStale)

        if (shouldGenerateSettlement) {
            _state.update { it.copy(isGenerating = true) }
            generateSettlement(groupId)
            details = observeGroupDetails(groupId)
        }

        return SettlementSnapshot(
            participants = details.participants,
            balances = calculateGroupBalances(groupId),
            settlement = details.latestSettlement,
            currentSourceRevision = details.currentSourceRevision,
            isSettlementStale = details.isSettlementStale,
            canGenerateSettlement = canGenerateSettlement,
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
            recordingTransferId = null,
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
