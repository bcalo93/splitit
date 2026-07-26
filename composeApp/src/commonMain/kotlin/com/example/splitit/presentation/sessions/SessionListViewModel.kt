package com.example.splitit.presentation.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitit.domain.model.ExpenseSession
import com.example.splitit.domain.usecase.DeleteSessionUseCase
import com.example.splitit.domain.usecase.ObserveSessionsUseCase
import com.example.splitit.domain.value.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionListUiState(
    val sessions: List<ExpenseSession> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class SessionListViewModel(
    private val observeSessions: ObserveSessionsUseCase,
    private val deleteSession: DeleteSessionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionListUiState())
    val state: StateFlow<SessionListUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { observeSessions() }
                .onSuccess { sessions ->
                    _state.update {
                        it.copy(
                            sessions = sessions,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Could not load sessions.",
                        )
                    }
                }
        }
    }

    fun delete(sessionId: SessionId) {
        viewModelScope.launch {
            runCatching { deleteSession(sessionId) }
                .onSuccess { refresh() }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(errorMessage = throwable.message ?: "Could not delete the session.")
                    }
                }
        }
    }
}
