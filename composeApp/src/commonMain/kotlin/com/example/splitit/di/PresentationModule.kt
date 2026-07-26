package com.example.splitit.di

import com.example.splitit.domain.value.SessionId
import com.example.splitit.presentation.participants.ParticipantsViewModel
import com.example.splitit.presentation.sessiondetail.SessionDetailsViewModel
import com.example.splitit.presentation.sessions.SessionFormViewModel
import com.example.splitit.presentation.sessions.SessionListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { SessionListViewModel(get(), get()) }
    viewModel { (sessionId: SessionId?) -> SessionFormViewModel(sessionId, get(), get(), get()) }
    viewModel { (sessionId: SessionId) -> SessionDetailsViewModel(sessionId, get()) }
    viewModel { (sessionId: SessionId) -> ParticipantsViewModel(sessionId, get(), get(), get(), get()) }
}
