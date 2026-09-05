package com.splitit.presentation.groups

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.usecase.CreateGroupParams
import com.splitit.domain.usecase.CreateGroupUseCase
import com.splitit.domain.usecase.GroupDetails
import com.splitit.domain.usecase.ObserveGroupDetailsParams
import com.splitit.domain.usecase.UpdateGroupParams
import com.splitit.domain.usecase.UpdateGroupUseCase
import com.splitit.domain.usecase.UseCase
import com.splitit.domain.value.GroupId
import com.splitit.localization.LocalizedString
import com.splitit.localization.LocalizationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class GroupFormUiState(
    val title: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val errorMessage: String? = null,
    val savedGroupId: GroupId? = null,
)

class GroupFormViewModel(
    private val groupId: GroupId?,
    private val createGroup: CreateGroupUseCase,
    private val updateGroup: UpdateGroupUseCase,
    private val observeGroupDetails: UseCase<ObserveGroupDetailsParams, GroupDetails>,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(GroupFormUiState(isLoading = groupId != null))
    val state: StateFlow<GroupFormUiState> = _state.asStateFlow()

    init {
        if (groupId != null) {
            load(groupId)
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

    fun onTitleBlur() {
        if (_state.value.title.isBlank()) {
            _state.update { it.copy(titleError = localization.getString(LocalizedString.ErrorEnterGroupName)) }
        }
    }

    fun save() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(titleError = localization.getString(LocalizedString.ErrorEnterGroupName)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val result = runCatching {
                if (groupId == null) {
                    createGroup(CreateGroupParams(current.title, current.description))
                } else {
                    updateGroup(UpdateGroupParams(groupId, current.title, current.description))
                }
            }

            result
                .onSuccess { group ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            savedGroupId = group.id,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotSaveGroup),
                        )
                    }
                }
        }
    }

    fun consumeSavedGroup() {
        _state.update { it.copy(savedGroupId = null) }
    }

    private fun load(groupId: GroupId) {
        viewModelScope.launch {
            runCatching { observeGroupDetails(ObserveGroupDetailsParams(groupId)).group }
                .onSuccess { group ->
                    _state.update {
                        it.copy(
                            title = group.title,
                            description = group.description.orEmpty(),
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: localization.getString(LocalizedString.ErrorCouldNotLoadGroup),
                        )
                    }
                }
        }
    }
}
