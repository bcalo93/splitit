package com.example.splitit.presentation.sessiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitit.domain.usecase.SessionDetails
import com.example.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.example.splitit.domain.value.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionDetailsUiState(
    val details: SessionDetails? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class SessionDetailsViewModel(
    private val sessionId: SessionId,
    private val observeSessionDetails: ObserveSessionDetailsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionDetailsUiState())
    val state: StateFlow<SessionDetailsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { observeSessionDetails(sessionId) }
                .onSuccess { details ->
                    _state.update {
                        it.copy(
                            details = details,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Could not load session details.",
                        )
                    }
                }
        }
    }
}
