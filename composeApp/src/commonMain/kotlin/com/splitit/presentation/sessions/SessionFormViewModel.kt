package com.splitit.presentation.sessions

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.usecase.CreateSessionUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.usecase.UpdateSessionUseCase
import com.splitit.domain.value.SessionId
import com.splitit.localization.LocalizationKey
import com.splitit.localization.LocalizationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SessionFormUiState(
    val title: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val errorMessage: String? = null,
    val savedSessionId: SessionId? = null,
)

class SessionFormViewModel(
    private val sessionId: SessionId?,
    private val createSession: CreateSessionUseCase,
    private val updateSession: UpdateSessionUseCase,
    private val observeSessionDetails: ObserveSessionDetailsUseCase,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionFormUiState(isLoading = sessionId != null))
    val state: StateFlow<SessionFormUiState> = _state.asStateFlow()

    init {
        if (sessionId != null) {
            load(sessionId)
        }
    }

    fun onTitleChange(title: String) {
        _state.update {
            it.copy(
                title = title,
                titleError = null,
                errorMessage = null,
            )
        }
    }

    fun onDescriptionChange(description: String) {
        _state.update { it.copy(description = description, errorMessage = null) }
    }

    fun save() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(titleError = localization.getString(LocalizationKey.ErrorEnterSessionName)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val result = runCatching {
                if (sessionId == null) {
                    createSession(current.title, current.description)
                } else {
                    updateSession(sessionId, current.title, current.description)
                }
            }

            result
                .onSuccess { session ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            savedSessionId = session.id,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: localization.getString(LocalizationKey.ErrorCouldNotSaveSession),
                        )
                    }
                }
        }
    }

    fun consumeSavedSession() {
        _state.update { it.copy(savedSessionId = null) }
    }

    private fun load(sessionId: SessionId) {
        viewModelScope.launch {
            runCatching { observeSessionDetails(sessionId).session }
                .onSuccess { session ->
                    _state.update {
                        it.copy(
                            title = session.title,
                            description = session.description.orEmpty(),
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: localization.getString(LocalizationKey.ErrorCouldNotLoadSession),
                        )
                    }
                }
        }
    }
}
