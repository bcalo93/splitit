package com.splitit.presentation.participants

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.model.Participant
import com.splitit.domain.usecase.AddParticipantUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.usecase.RemoveParticipantUseCase
import com.splitit.domain.usecase.UpdateParticipantUseCase
import com.splitit.domain.value.ParticipantId
import com.splitit.domain.value.SessionId
import com.splitit.localization.LocalizedString
import com.splitit.localization.LocalizationService
import com.splitit.ui.theme.SplitItAvatarColorHexes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ParticipantsUiState(
    val participants: List<Participant> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val selectedColor: String = ParticipantColors.first(),
    val editingParticipantId: ParticipantId? = null,
    val nameError: String? = null,
    val errorMessage: String? = null,
)

val ParticipantColors: List<String> = SplitItAvatarColorHexes

class ParticipantsViewModel(
    private val sessionId: SessionId,
    private val observeSessionDetails: ObserveSessionDetailsUseCase,
    private val addParticipant: AddParticipantUseCase,
    private val updateParticipant: UpdateParticipantUseCase,
    private val removeParticipant: RemoveParticipantUseCase,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(ParticipantsUiState())
    val state: StateFlow<ParticipantsUiState> = _state.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val participants = observeSessionDetails(sessionId).participants
                _state.update {
                    it.copy(
                        participants = participants,
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
                        errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotLoadParticipants),
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = null,
                errorMessage = null,
            )
        }
    }

    fun onColorSelected(color: String) {
        _state.update { it.copy(selectedColor = color, errorMessage = null) }
    }

    fun startEditing(participant: Participant) {
        _state.update {
            it.copy(
                name = participant.name,
                selectedColor = participant.avatarColor ?: ParticipantColors.first(),
                editingParticipantId = participant.id,
                nameError = null,
                errorMessage = null,
            )
        }
    }

    fun cancelEditing() {
        _state.update { it.emptyForm() }
    }

    fun save() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(nameError = localization.getString(LocalizedString.ErrorEnterParticipantName)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val result = runCatching {
                val editingId = current.editingParticipantId
                if (editingId == null) {
                    addParticipant(sessionId, current.name, current.selectedColor)
                } else {
                    updateParticipant(editingId, current.name, current.selectedColor)
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
                            errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotSaveParticipant),
                        )
                    }
                }
        }
    }

    fun delete(participantId: ParticipantId) {
        viewModelScope.launch {
            runCatching { removeParticipant(participantId) }
                .onSuccess { refresh() }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            errorMessage = if (throwable is IllegalArgumentException) {
                                localization.getString(LocalizedString.ErrorParticipantUsedByExpenses)
                            } else {
                                throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotRemoveParticipant)
                            },
                        )
                    }
                }
        }
    }

    private fun ParticipantsUiState.emptyForm(): ParticipantsUiState {
        return copy(
            name = "",
            selectedColor = ParticipantColors.first(),
            editingParticipantId = null,
            nameError = null,
            errorMessage = null,
        )
    }
}
