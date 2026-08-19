package com.splitit.presentation.groupdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.model.Expense
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.value.Money
import com.splitit.domain.value.GroupId
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
data class GroupDetailsUiState(
    val details: GroupDetails? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val totalSpent: Money?
        get() = details?.expenses?.totalSpent()
}

private fun List<Expense>.totalSpent(): Money? {
    if (isEmpty()) return null
    val currency = first().amount.currencyCode
    return map { it.amount }
        .filter { it.currencyCode == currency }
        .fold(Money.zero(currency)) { acc, amount -> acc + amount }
}

class GroupDetailsViewModel(
    private val groupId: GroupId,
    private val observeGroupDetails: ObserveGroupDetailsUseCase,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(GroupDetailsUiState())
    val state: StateFlow<GroupDetailsUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val details = observeGroupDetails(groupId)
                _state.update {
                    it.copy(
                        details = details,
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
                        errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotLoadGroupDetails),
                    )
                }
            }
        }
    }
}
