package com.splitit.domain.repository

data class AppSettings(
    val defaultCurrencyCode: String = "USD",
    val themeMode: ThemeMode = ThemeMode.System,
)

enum class ThemeMode {
    System,
    Light,
    Dark,
}

interface SettingsRepository {
    suspend fun getSettings(): AppSettings
    suspend fun saveSettings(settings: AppSettings)
}
