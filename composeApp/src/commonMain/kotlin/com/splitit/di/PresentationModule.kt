package com.splitit.di

import com.splitit.domain.value.SessionId
import com.splitit.presentation.expenses.ExpensesViewModel
import com.splitit.presentation.participants.ParticipantsViewModel
import com.splitit.presentation.sessiondetail.SessionDetailsViewModel
import com.splitit.presentation.sessions.SessionFormViewModel
import com.splitit.presentation.sessions.SessionListViewModel
import com.splitit.presentation.settlement.SettlementViewModel
import com.splitit.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { SessionListViewModel(get(), get(), get()) }
    viewModel { (sessionId: SessionId?) -> SessionFormViewModel(sessionId, get(), get(), get(), get()) }
    viewModel { (sessionId: SessionId) -> SessionDetailsViewModel(sessionId, get(), get()) }
    viewModel { (sessionId: SessionId) -> ParticipantsViewModel(sessionId, get(), get(), get(), get(), get()) }
    viewModel { (sessionId: SessionId) -> ExpensesViewModel(sessionId, get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (sessionId: SessionId) -> SettlementViewModel(sessionId, get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}
