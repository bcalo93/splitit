package com.example.splitit.presentation.participants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.usecase.AddParticipantUseCase
import com.example.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.example.splitit.domain.usecase.RemoveParticipantUseCase
import com.example.splitit.domain.usecase.UpdateParticipantUseCase
import com.example.splitit.domain.value.ParticipantId
import com.example.splitit.domain.value.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

val ParticipantColors = listOf(
    "#2F80ED",
    "#27AE60",
    "#EB5757",
    "#F2994A",
    "#9B51E0",
    "#00A6A6",
)

class ParticipantsViewModel(
    private val sessionId: SessionId,
    private val observeSessionDetails: ObserveSessionDetailsUseCase,
    private val addParticipant: AddParticipantUseCase,
    private val updateParticipant: UpdateParticipantUseCase,
    private val removeParticipant: RemoveParticipantUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ParticipantsUiState())
    val state: StateFlow<ParticipantsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { observeSessionDetails(sessionId).participants }
                .onSuccess { participants ->
                    _state.update {
                        it.copy(
                            participants = participants,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Could not load participants.",
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
            _state.update { it.copy(nameError = "Enter a participant name.") }
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
                            errorMessage = throwable.message ?: "Could not save participant.",
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
                                "Participant cannot be removed because it is used by expenses."
                            } else {
                                throwable.message ?: "Could not remove participant."
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
