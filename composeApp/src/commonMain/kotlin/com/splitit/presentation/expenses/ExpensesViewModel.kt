package com.splitit.presentation.expenses

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.model.Expense
import com.splitit.domain.model.Participant
import com.splitit.domain.usecase.CreateExpenseUseCase
import com.splitit.domain.usecase.DeleteExpenseUseCase
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.usecase.UpdateExpenseUseCase
import com.splitit.domain.value.Clock
import com.splitit.domain.value.ExpenseId
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId
import com.splitit.localization.LocalizationKey
import com.splitit.localization.LocalizationService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ExpensesUiState(
    val participants: List<Participant> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val visibleExpenses: List<Expense> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val defaultCurrencyCode: String = DefaultCurrencyCode,
    val title: String = "",
    val amount: String = "",
    val payerId: ParticipantId? = null,
    val selectedParticipantIds: Set<ParticipantId> = emptySet(),
    val note: String = "",
    val editingExpenseId: ExpenseId? = null,
    val editingCurrencyCode: String? = null,
    val titleError: String? = null,
    val amountError: String? = null,
    val payerError: String? = null,
    val participantsError: String? = null,
    val errorMessage: String? = null,
)

class ExpensesViewModel(
    private val sessionId: SessionId,
    private val observeSessionDetails: ObserveSessionDetailsUseCase,
    private val createExpense: CreateExpenseUseCase,
    private val updateExpense: UpdateExpenseUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    private val clock: Clock,
    private val getSettings: GetSettingsUseCase,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(ExpensesUiState())
    val state: StateFlow<ExpensesUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val (details, settings) = observeSessionDetails(sessionId) to getSettings()
                _state.update {
                    val current = it
                    val defaultPayer = current.payerId ?: details.participants.firstOrNull()?.id
                    val selected = current.selectedParticipantIds.ifEmpty {
                        defaultPayer?.let { setOf(it) } ?: emptySet()
                    }

                    current.copy(
                        participants = details.participants,
                        expenses = details.expenses,
                        visibleExpenses = filterExpenses(details.expenses, current.searchQuery),
                        defaultCurrencyCode = settings.defaultCurrencyCode,
                        payerId = defaultPayer,
                        selectedParticipantIds = selected,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: localization.getString(LocalizationKey.ErrorCouldNotLoadExpenses),
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                visibleExpenses = filterExpenses(it.expenses, query),
                errorMessage = null,
            )
        }
    }

    fun onTitleChange(title: String) {
        _state.update { it.copy(title = title, titleError = null, errorMessage = null) }
    }

    fun onAmountChange(amount: String) {
        _state.update { it.copy(amount = amount, amountError = null, errorMessage = null) }
    }

    fun onNoteChange(note: String) {
        _state.update { it.copy(note = note, errorMessage = null) }
    }

    fun onPayerSelected(payerId: ParticipantId) {
        _state.update {
            it.copy(
                payerId = payerId,
                selectedParticipantIds = it.selectedParticipantIds + payerId,
                payerError = null,
                participantsError = null,
                errorMessage = null,
            )
        }
    }

    fun onParticipantToggled(participantId: ParticipantId) {
        _state.update {
            val selected = if (participantId in it.selectedParticipantIds) {
                it.selectedParticipantIds - participantId
            } else {
                it.selectedParticipantIds + participantId
            }
            it.copy(
                selectedParticipantIds = selected,
                participantsError = null,
                errorMessage = null,
            )
        }
    }

    fun startEditing(expense: Expense) {
        _state.update {
            it.copy(
                title = expense.title,
                amount = formatMinorUnits(expense.amount.minorUnits),
                payerId = expense.payerId,
                selectedParticipantIds = expense.participantShares.map { share -> share.participantId }.toSet(),
                note = expense.note.orEmpty(),
                editingExpenseId = expense.id,
                editingCurrencyCode = expense.amount.currencyCode,
                titleError = null,
                amountError = null,
                payerError = null,
                participantsError = null,
                errorMessage = null,
            )
        }
    }

    fun cancelEditing() {
        _state.update { it.emptyForm() }
    }

    fun save() {
        val current = _state.value
        val parsedAmount = parseAmount(current.amount)
        val payerId = current.payerId
        var hasError = false

        if (current.title.isBlank()) {
            hasError = true
            _state.update { it.copy(titleError = localization.getString(LocalizationKey.ErrorEnterExpenseTitle)) }
        }
        if (parsedAmount == null || parsedAmount <= 0) {
            hasError = true
            _state.update { it.copy(amountError = localization.getString(LocalizationKey.ErrorEnterPositiveAmount)) }
        }
        if (payerId == null) {
            hasError = true
            _state.update { it.copy(payerError = localization.getString(LocalizationKey.ErrorChoosePayer)) }
        }
        if (current.selectedParticipantIds.isEmpty()) {
            hasError = true
            _state.update { it.copy(participantsError = localization.getString(LocalizationKey.ErrorChooseAtLeastOneParticipant)) }
        }
        if (hasError || parsedAmount == null || payerId == null) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val result = runCatching {
                val editingId = current.editingExpenseId
                val currencyCode = if (editingId == null) {
                    getSettings().defaultCurrencyCode
                } else {
                    current.editingCurrencyCode
                        ?: current.expenses.firstOrNull { it.id == editingId }?.amount?.currencyCode
                        ?: current.defaultCurrencyCode
                }
                if (editingId == null) {
                    createExpense(
                        sessionId = sessionId,
                        title = current.title,
                        amount = Money(parsedAmount, currencyCode),
                        payerId = payerId,
                        participantIds = current.selectedParticipantIds.toList(),
                        dateMillis = clock.nowMillis(),
                        note = current.note,
                    )
                } else {
                    updateExpense(
                        expenseId = editingId,
                        title = current.title,
                        amount = Money(parsedAmount, currencyCode),
                        payerId = payerId,
                        participantIds = current.selectedParticipantIds.toList(),
                        dateMillis = current.expenses.first { it.id == editingId }.dateMillis,
                        note = current.note,
                    )
                }
            }

            result
                .onSuccess {
                    _state.update { it.emptyForm().copy(isSaving = false) }
                    refresh()
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: localization.getString(LocalizationKey.ErrorCouldNotSaveExpense),
                        )
                    }
                }
        }
    }

    fun delete(expenseId: ExpenseId) {
        viewModelScope.launch {
            runCatching { deleteExpense(expenseId) }
                .onSuccess {
                    if (_state.value.editingExpenseId == expenseId) {
                        _state.update { it.emptyForm() }
                    }
                    refresh()
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(errorMessage = throwable.message ?: localization.getString(LocalizationKey.ErrorCouldNotDeleteExpense))
                    }
                }
        }
    }

    private fun filterExpenses(
        expenses: List<Expense>,
        query: String,
    ): List<Expense> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return expenses

        return expenses.filter { expense ->
            expense.title.contains(normalizedQuery, ignoreCase = true) ||
                expense.note?.contains(normalizedQuery, ignoreCase = true) == true
        }
    }

    private fun ExpensesUiState.emptyForm(): ExpensesUiState {
        val defaultPayer = participants.firstOrNull()?.id
        return copy(
            title = "",
            amount = "",
            payerId = defaultPayer,
            selectedParticipantIds = defaultPayer?.let { setOf(it) } ?: emptySet(),
            note = "",
            editingExpenseId = null,
            editingCurrencyCode = null,
            titleError = null,
            amountError = null,
            payerError = null,
            participantsError = null,
            errorMessage = null,
        )
    }
}

const val DefaultCurrencyCode = "USD"

fun parseAmount(input: String): Long? {
    val normalized = input.trim()
    if (normalized.isEmpty()) return null
    val parts = normalized.split(".")
    if (parts.size > 2 || parts.any { it.isEmpty() }) return null

    val major = parts[0].toLongOrNull() ?: return null
    if (major < 0) return null

    val minor = when (parts.size) {
        1 -> 0
        else -> {
            val decimals = parts[1]
            if (decimals.length > 2 || decimals.any { !it.isDigit() }) return null
            decimals.padEnd(2, '0').toLong()
        }
    }

    return major * 100 + minor
}

fun formatMinorUnits(minorUnits: Long): String {
    val major = minorUnits / 100
    val minor = minorUnits % 100
    return if (minor == 0L) {
        major.toString()
    } else {
        "$major.${minor.toString().padStart(2, '0')}"
    }
}
