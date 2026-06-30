package com.example.splitit.data.repository

import com.example.splitit.data.database.SplitItDatabase
import com.example.splitit.data.mapper.toDomain
import com.example.splitit.domain.repository.AppSettings
import com.example.splitit.domain.repository.SettingsRepository

class SqlDelightSettingsRepository(
    database: SplitItDatabase,
) : SettingsRepository {
    private val queries = database.splitItDatabaseQueries

    override suspend fun getSettings(): AppSettings {
        return queries.selectSettings().executeAsOneOrNull()?.toDomain() ?: AppSettings()
    }

    override suspend fun saveSettings(settings: AppSettings) {
        queries.upsertSettings(
            default_currency_code = settings.defaultCurrencyCode,
            theme_mode = settings.themeMode.name,
        )
    }
}
