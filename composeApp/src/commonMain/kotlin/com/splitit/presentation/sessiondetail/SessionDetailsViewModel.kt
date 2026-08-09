package com.splitit.presentation.sessiondetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.usecase.SessionDetails
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
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val details = observeSessionDetails(sessionId)
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
                        errorMessage = throwable.message ?: "Could not load session details.",
                    )
                }
            }
        }
    }
}
