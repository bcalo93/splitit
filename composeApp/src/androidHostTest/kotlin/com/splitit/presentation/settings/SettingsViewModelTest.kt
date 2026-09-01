@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.splitit.presentation.settings

import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.ThemeMode
import com.splitit.domain.usecase.GetSettingsParams
import com.splitit.domain.usecase.SaveSettingsParams
import com.splitit.testutils.runViewModelTest
import com.splitit.testutils.testLocalizationService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsViewModelTest {
    @Test
    fun loadsAndSavesNormalizedCurrencyAndTheme() = runViewModelTest {
        val getSettings = mockk<com.splitit.domain.usecase.GetSettingsUseCase>()
        coEvery { getSettings.invoke(GetSettingsParams) } returns AppSettings()
        val saveSettings = mockk<com.splitit.domain.usecase.SaveSettingsUseCase>()
        coEvery { saveSettings.invoke(any()) } returns Unit

        val viewModel = SettingsViewModel(
            getSettings = getSettings,
            saveSettings = saveSettings,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        assertEquals("UYU", viewModel.state.value.settings.defaultCurrencyCode)
        viewModel.onCurrencyCodeChange("eur123")
        viewModel.onThemeModeSelected(ThemeMode.Dark)
        assertEquals("EUR", viewModel.state.value.draftSettings.defaultCurrencyCode)
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            saveSettings.invoke(SaveSettingsParams(AppSettings("EUR", ThemeMode.Dark)))
        }
        assertTrue(viewModel.state.value.saveCompleted)
        viewModel.consumeSaveCompleted()
        assertFalse(viewModel.state.value.saveCompleted)
    }

    @Test
    fun rejectsInvalidCurrencyWithoutSaving() = runViewModelTest {
        val saveSettings = mockk<com.splitit.domain.usecase.SaveSettingsUseCase>()
        val getSettings = mockk<com.splitit.domain.usecase.GetSettingsUseCase>()
        coEvery { getSettings.invoke(any()) } returns AppSettings()
        val viewModel = SettingsViewModel(
            getSettings = getSettings,
            saveSettings = saveSettings,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        viewModel.onCurrencyCodeChange("US")
        viewModel.save()

        assertEquals(
            "Use a 3-letter currency code, such as USD or EUR.",
            viewModel.state.value.currencyError,
        )
        coVerify(exactly = 0) { saveSettings.invoke(any()) }
    }

    @Test
    fun exposesSaveErrors() = runViewModelTest {
        val saveSettings = mockk<com.splitit.domain.usecase.SaveSettingsUseCase>()
        coEvery { saveSettings.invoke(any()) } throws IllegalStateException("settings unavailable")
        val getSettings = mockk<com.splitit.domain.usecase.GetSettingsUseCase>()
        coEvery { getSettings.invoke(any()) } returns AppSettings()

        val viewModel = SettingsViewModel(
            getSettings = getSettings,
            saveSettings = saveSettings,
            localization = testLocalizationService,
        )
        advanceUntilIdle()

        viewModel.onCurrencyCodeChange("EUR")
        viewModel.save()
        advanceUntilIdle()

        assertEquals("settings unavailable", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.saveCompleted)
    }
}
