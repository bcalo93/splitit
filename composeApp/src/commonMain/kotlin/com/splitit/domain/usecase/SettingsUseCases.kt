package com.splitit.domain.usecase

import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.SettingsRepository

class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): AppSettings {
        return settingsRepository.getSettings()
    }
}

class SaveSettingsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(settings: AppSettings) {
        settingsRepository.saveSettings(settings)
    }
}
