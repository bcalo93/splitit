package com.splitit.domain.repository

data class AppSettings(
    val defaultCurrencyCode: String = DefaultCurrencyCode,
    val themeMode: ThemeMode = ThemeMode.System,
)

const val DefaultCurrencyCode = "UYU"

enum class ThemeMode {
    System,
    Light,
    Dark,
}

interface SettingsRepository {
    suspend fun getSettings(): AppSettings
    suspend fun saveSettings(settings: AppSettings)
}
