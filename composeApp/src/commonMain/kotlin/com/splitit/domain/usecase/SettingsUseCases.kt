package com.splitit.domain.usecase

import com.splitit.domain.repository.AppSettings
import com.splitit.domain.repository.SettingsRepository

object GetSettingsParams

class GetSettingsUseCase(
    private val settingsRepository: SettingsRepository,
) : UseCase<GetSettingsParams, AppSettings> {
    override suspend fun invoke(params: GetSettingsParams): AppSettings {
        return settingsRepository.getSettings()
    }
}

data class SaveSettingsParams(
    val settings: AppSettings,
)

class SaveSettingsUseCase(
    private val settingsRepository: SettingsRepository,
) : UseCase<SaveSettingsParams, Unit> {
    override suspend fun invoke(params: SaveSettingsParams) {
        settingsRepository.saveSettings(params.settings)
    }
}
