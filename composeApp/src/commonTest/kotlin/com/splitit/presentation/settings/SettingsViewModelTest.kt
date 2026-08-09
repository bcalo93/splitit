@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.settings

import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.SaveSettingsUseCase
import com.splitit.testutils.InMemorySettingsRepository
import com.splitit.testutils.runViewModelTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsViewModelTest {
    @Test
    fun loadsAndSavesNormalizedCurrencyAndTheme() = runViewModelTest {
        val repository = InMemorySettingsRepository()
        val viewModel = SettingsViewModel(
            getSettings = GetSettingsUseCase(repository),
            saveSettings = SaveSettingsUseCase(repository),
        )
        advanceUntilIdle()

        viewModel.onCurrencyCodeChange("eur123")
        viewModel.onThemeModeSelected(ThemeMode.Dark)
        assertEquals("EUR", viewModel.state.value.draftSettings.defaultCurrencyCode)
        viewModel.save()
        advanceUntilIdle()

        assertEquals(AppSettings("EUR", ThemeMode.Dark), repository.settings)
        assertTrue(viewModel.state.value.saveCompleted)
        viewModel.consumeSaveCompleted()
        assertEquals(false, viewModel.state.value.saveCompleted)
    }

    @Test
    fun rejectsInvalidCurrencyWithoutSaving() = runViewModelTest {
        val repository = InMemorySettingsRepository()
        val viewModel = SettingsViewModel(
            getSettings = GetSettingsUseCase(repository),
            saveSettings = SaveSettingsUseCase(repository),
        )
        advanceUntilIdle()

        viewModel.onCurrencyCodeChange("US")
        viewModel.save()

        assertEquals("Use a 3-letter currency code, such as USD or EUR.", viewModel.state.value.currencyError)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun exposesSaveErrors() = runViewModelTest {
        val repository = InMemorySettingsRepository()
        repository.saveError = IllegalStateException("settings unavailable")
        val viewModel = SettingsViewModel(
            getSettings = GetSettingsUseCase(repository),
            saveSettings = SaveSettingsUseCase(repository),
        )
        advanceUntilIdle()

        viewModel.onCurrencyCodeChange("EUR")
        viewModel.save()
        advanceUntilIdle()

        assertEquals("settings unavailable", viewModel.state.value.errorMessage)
        assertEquals(false, viewModel.state.value.saveCompleted)
    }
}
