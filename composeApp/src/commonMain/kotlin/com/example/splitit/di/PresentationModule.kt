package com.example.splitit.di

import com.example.splitit.domain.value.SessionId
import com.example.splitit.presentation.expenses.ExpensesViewModel
import com.example.splitit.presentation.participants.ParticipantsViewModel
import com.example.splitit.presentation.sessiondetail.SessionDetailsViewModel
import com.example.splitit.presentation.sessions.SessionFormViewModel
import com.example.splitit.presentation.sessions.SessionListViewModel
import com.example.splitit.presentation.settlement.SettlementViewModel
import com.example.splitit.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { SessionListViewModel(get(), get()) }
    viewModel { (sessionId: SessionId?) -> SessionFormViewModel(sessionId, get(), get(), get()) }
    viewModel { (sessionId: SessionId) -> SessionDetailsViewModel(sessionId, get()) }
    viewModel { (sessionId: SessionId) -> ParticipantsViewModel(sessionId, get(), get(), get(), get()) }
    viewModel { (sessionId: SessionId) -> ExpensesViewModel(sessionId, get(), get(), get(), get(), get(), get()) }
    viewModel { (sessionId: SessionId) -> SettlementViewModel(sessionId, get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
