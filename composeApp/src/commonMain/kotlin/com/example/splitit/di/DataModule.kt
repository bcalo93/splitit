package com.example.splitit.di

import com.example.splitit.data.database.DatabaseDriverFactory
import com.example.splitit.data.database.createDatabase
import com.example.splitit.data.repository.SqlDelightExpenseRepository
import com.example.splitit.data.repository.SqlDelightParticipantRepository
import com.example.splitit.data.repository.SqlDelightSessionRepository
import com.example.splitit.data.repository.SqlDelightSettingsRepository
import com.example.splitit.data.repository.SqlDelightSettlementRepository
import com.example.splitit.domain.repository.ExpenseRepository
import com.example.splitit.domain.repository.ParticipantRepository
import com.example.splitit.domain.repository.SessionRepository
import com.example.splitit.domain.repository.SettingsRepository
import com.example.splitit.domain.repository.SettlementRepository
import org.koin.dsl.module

fun dataModule(databaseDriverFactory: DatabaseDriverFactory) = module {
    single { createDatabase(databaseDriverFactory) }
    single<SessionRepository> { SqlDelightSessionRepository(get()) }
    single<ParticipantRepository> { SqlDelightParticipantRepository(get()) }
    single<ExpenseRepository> { SqlDelightExpenseRepository(get()) }
    single<SettlementRepository> { SqlDelightSettlementRepository(get()) }
    single<SettingsRepository> { SqlDelightSettingsRepository(get()) }
}
