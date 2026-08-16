package com.splitit.presentation.sessions

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.model.ExpenseSession
import com.splitit.domain.usecase.DeleteSessionUseCase
import com.splitit.domain.usecase.ObserveSessionsUseCase
import com.splitit.domain.value.SessionId
import com.splitit.localization.LocalizationService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SessionListUiState(
    val sessions: List<ExpenseSession> = emptyList(),
    val visibleSessions: List<ExpenseSession> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class SessionListViewModel(
    private val observeSessions: ObserveSessionsUseCase,
    private val deleteSession: DeleteSessionUseCase,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionListUiState())
    val state: StateFlow<SessionListUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val sessions = observeSessions()
                _state.update {
                    it.copy(
                        sessions = sessions,
                        visibleSessions = filterSessions(sessions, it.searchQuery),
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
                        errorMessage = throwable.message ?: localization.getString("error_could_not_load_sessions"),
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                visibleSessions = filterSessions(it.sessions, query),
                errorMessage = null,
            )
        }
    }

    fun delete(sessionId: SessionId) {
        viewModelScope.launch {
            try {
                deleteSession(sessionId)
                refresh()
            } catch (exception: CancellationException) {
                throw exception
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(errorMessage = throwable.message ?: localization.getString("error_could_not_delete_session"))
                }
            }
        }
    }

    private fun filterSessions(
        sessions: List<ExpenseSession>,
        query: String,
    ): List<ExpenseSession> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return sessions

        return sessions.filter { session ->
            session.title.contains(normalizedQuery, ignoreCase = true) ||
                session.description?.contains(normalizedQuery, ignoreCase = true) == true
        }
    }
}
