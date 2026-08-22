package com.splitit.di

import com.splitit.data.database.DatabaseDriverFactory
import com.splitit.data.database.createDatabase
import com.splitit.data.repository.SqlDelightExpenseRepository
import com.splitit.data.repository.SqlDelightParticipantRepository
import com.splitit.data.repository.SqlDelightGroupRepository
import com.splitit.data.repository.SqlDelightSettingsRepository
import com.splitit.data.repository.SqlDelightSettlementRepository
import com.splitit.domain.repository.ExpenseRepository
import com.splitit.domain.repository.ParticipantRepository
import com.splitit.domain.repository.GroupRepository
import com.splitit.domain.repository.SettingsRepository
import com.splitit.domain.repository.SettlementRepository
import org.koin.dsl.module

fun dataModule(databaseDriverFactory: DatabaseDriverFactory) = module {
    single { createDatabase(databaseDriverFactory) }
    single<GroupRepository> { SqlDelightGroupRepository(get()) }
    single<ParticipantRepository> { SqlDelightParticipantRepository(get()) }
    single<ExpenseRepository> { SqlDelightExpenseRepository(get()) }
    single<SettlementRepository> { SqlDelightSettlementRepository(get()) }
    single<SettingsRepository> { SqlDelightSettingsRepository(get()) }
}
