package com.splitit.di

import com.splitit.domain.value.GroupId
import com.splitit.presentation.expenses.ExpensesViewModel
import com.splitit.presentation.participants.ParticipantsViewModel
import com.splitit.presentation.groupdetail.GroupDetailsViewModel
import com.splitit.presentation.groups.GroupFormViewModel
import com.splitit.presentation.groups.GroupListViewModel
import com.splitit.presentation.settlement.SettlementViewModel
import com.splitit.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { GroupListViewModel(get(), get(), get(), get()) }
    viewModel { (groupId: GroupId?) -> GroupFormViewModel(groupId, get(), get(), get(), get()) }
    viewModel { (groupId: GroupId) -> GroupDetailsViewModel(groupId, get(), get()) }
    viewModel { (groupId: GroupId) -> ParticipantsViewModel(groupId, get(), get(), get(), get(), get()) }
    viewModel { (groupId: GroupId) -> ExpensesViewModel(groupId, get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (groupId: GroupId) -> SettlementViewModel(groupId, get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}
