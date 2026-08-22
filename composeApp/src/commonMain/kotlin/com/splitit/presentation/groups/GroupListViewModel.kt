package com.splitit.presentation.groups

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.model.ExpenseGroup
import com.splitit.domain.model.Participant
import com.splitit.domain.usecase.DeleteGroupUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.ObserveGroupsUseCase
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
data class GroupListUiState(
    val groups: List<ExpenseGroup> = emptyList(),
    val visibleGroups: List<ExpenseGroup> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val participantsByGroup: Map<GroupId, List<Participant>> = emptyMap(),
    val pendingGroupIds: Set<GroupId> = emptySet(),
)

class GroupListViewModel(
    private val observeGroups: ObserveGroupsUseCase,
    private val observeGroupDetails: ObserveGroupDetailsUseCase,
    private val deleteGroup: DeleteGroupUseCase,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(GroupListUiState())
    val state: StateFlow<GroupListUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val groups = observeGroups()
                val participantsByGroup = mutableMapOf<GroupId, List<Participant>>()
                val pendingGroupIds = mutableSetOf<GroupId>()
                groups.forEach { group ->
                    try {
                        val details = observeGroupDetails(group.id)
                        participantsByGroup[group.id] = details.participants
                        val pending = details.isSettlementStale ||
                            (details.latestSettlement == null && details.expenses.isNotEmpty())
                        if (pending) {
                            pendingGroupIds.add(group.id)
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Throwable) {
                        // Enrichment is best-effort; the list still renders its counts.
                    }
                }
                _state.update {
                    it.copy(
                        groups = groups,
                        visibleGroups = filterGroups(groups, it.searchQuery),
                        participantsByGroup = participantsByGroup,
                        pendingGroupIds = pendingGroupIds,
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
                        errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotLoadGroups),
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                visibleGroups = filterGroups(it.groups, query),
                errorMessage = null,
            )
        }
    }

    fun delete(groupId: GroupId) {
        viewModelScope.launch {
            try {
                deleteGroup(groupId)
                refresh()
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotDeleteGroup))
                }
            }
        }
    }

    private fun filterGroups(
        groups: List<ExpenseGroup>,
        query: String,
    ): List<ExpenseGroup> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return groups

        return groups.filter { group ->
            group.title.contains(normalizedQuery, ignoreCase = true) ||
                group.description?.contains(normalizedQuery, ignoreCase = true) == true
        }
    }
}
