package com.splitit.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.SaveSettingsUseCase
import com.splitit.localization.LocalizationKey
import com.splitit.localization.LocalizationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val draftSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val currencyError: String? = null,
    val errorMessage: String? = null,
    val saveCompleted: Boolean = false,
)

class SettingsViewModel(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val localization: LocalizationService,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { getSettings() }
                .onSuccess { settings ->
                    _state.update {
                        it.copy(
                            settings = settings,
                            draftSettings = settings,
                            isLoading = false,
                            currencyError = null,
                            errorMessage = null,
                            saveCompleted = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: localization.getString(LocalizationKey.ErrorCouldNotLoadSettings),
                        )
                    }
                }
        }
    }

    fun onCurrencyCodeChange(currencyCode: String) {
        _state.update {
            it.copy(
                draftSettings = it.draftSettings.copy(
                    defaultCurrencyCode = currencyCode.take(MAX_CURRENCY_CODE_LENGTH).uppercase(),
                ),
                currencyError = null,
                errorMessage = null,
                saveCompleted = false,
            )
        }
    }

    fun onThemeModeSelected(themeMode: ThemeMode) {
        _state.update {
            it.copy(
                draftSettings = it.draftSettings.copy(themeMode = themeMode),
                errorMessage = null,
                saveCompleted = false,
            )
        }
    }

    fun save() {
        val current = _state.value
        val currencyCode = current.draftSettings.defaultCurrencyCode.trim().uppercase()
        if (!isValidCurrencyCode(currencyCode)) {
            _state.update {
                it.copy(currencyError = localization.getString(LocalizationKey.ErrorInvalidCurrency))
            }
            return
        }

        val settings = current.draftSettings.copy(defaultCurrencyCode = currencyCode)
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSaving = true,
                    currencyError = null,
                    errorMessage = null,
                    saveCompleted = false,
                )
            }
            runCatching { saveSettings(settings) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            settings = settings,
                            draftSettings = settings,
                            isSaving = false,
                            errorMessage = null,
                            saveCompleted = true,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: localization.getString(LocalizationKey.ErrorCouldNotSaveSettings),
                        )
                    }
                }
        }
    }

    fun consumeSaveCompleted() {
        _state.update { it.copy(saveCompleted = false) }
    }

    private companion object {
        const val MAX_CURRENCY_CODE_LENGTH = 3

        fun isValidCurrencyCode(currencyCode: String): Boolean {
            return currencyCode.length == MAX_CURRENCY_CODE_LENGTH &&
                currencyCode.all { it in 'A'..'Z' }
        }
    }
}
