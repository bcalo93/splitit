package com.splitit.di

import com.splitit.domain.Payment
import com.splitit.domain.optimizer.PaymentOptimizerAdapter
import com.splitit.domain.service.BalanceCalculator
import com.splitit.localization.DefaultLocalizationService
import com.splitit.localization.LocalizationService
import com.splitit.domain.usecase.AddParticipantUseCase
import com.splitit.domain.usecase.CalculateGroupBalancesUseCase
import com.splitit.domain.usecase.CreateExpenseUseCase
import com.splitit.domain.usecase.CreateGroupUseCase
import com.splitit.domain.usecase.DeleteExpenseUseCase
import com.splitit.domain.usecase.DeleteGroupUseCase
import com.splitit.domain.usecase.GenerateSettlementUseCase
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.ObserveGroupDetailsUseCase
import com.splitit.domain.usecase.ObserveGroupsUseCase
import com.splitit.domain.usecase.RemoveParticipantUseCase
import com.splitit.domain.usecase.SaveSettingsUseCase
import com.splitit.domain.usecase.UpdateExpenseUseCase
import com.splitit.domain.usecase.UpdateParticipantUseCase
import com.splitit.domain.usecase.UpdateGroupUseCase
import com.splitit.domain.value.Clock
import com.splitit.domain.value.IdGenerator
import com.splitit.domain.value.SystemClock
import com.splitit.domain.value.UuidGenerator
import com.splitit.logic.optimizers.ComposedOptimizer
import com.splitit.logic.optimizers.Optimizer
import com.splitit.logic.optimizers.debt.CycleOptimizer
import com.splitit.logic.optimizers.debt.TransitiveOptimizer
import org.koin.dsl.module

val domainModule = module {
    single<IdGenerator> { UuidGenerator() }
    single<Clock> { SystemClock() }
    single { BalanceCalculator() }
    single { DefaultLocalizationService() }
    single<LocalizationService> { get<DefaultLocalizationService>() }

    single<Optimizer<Payment>> {
        ComposedOptimizer(
            listOf(
                CycleOptimizer(),
                TransitiveOptimizer(),
            ),
        )
    }
    single { PaymentOptimizerAdapter(get(), get()) }

    factory { CreateGroupUseCase(get(), get(), get()) }
    factory { UpdateGroupUseCase(get(), get()) }
    factory { DeleteGroupUseCase(get()) }
    factory { ObserveGroupsUseCase(get()) }
    factory { ObserveGroupDetailsUseCase(get(), get(), get(), get()) }

    factory { AddParticipantUseCase(get(), get(), get(), get()) }
    factory { UpdateParticipantUseCase(get(), get()) }
    factory { RemoveParticipantUseCase(get()) }

    factory { CreateExpenseUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateExpenseUseCase(get(), get(), get()) }
    factory { DeleteExpenseUseCase(get()) }

    factory { CalculateGroupBalancesUseCase(get(), get(), get()) }
    factory { GenerateSettlementUseCase(get(), get(), get(), get(), get(), get(), get()) }

    factory { GetSettingsUseCase(get()) }
    factory { SaveSettingsUseCase(get()) }
}
