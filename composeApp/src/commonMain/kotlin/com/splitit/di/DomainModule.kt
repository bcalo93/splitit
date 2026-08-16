package com.splitit.di

import com.splitit.domain.Payment
import com.splitit.domain.optimizer.PaymentOptimizerAdapter
import com.splitit.domain.service.BalanceCalculator
import com.splitit.localization.DefaultLocalizationService
import com.splitit.localization.DeviceLocale
import com.splitit.localization.LocalizationService
import com.splitit.domain.usecase.AddParticipantUseCase
import com.splitit.domain.usecase.CalculateSessionBalancesUseCase
import com.splitit.domain.usecase.CreateExpenseUseCase
import com.splitit.domain.usecase.CreateSessionUseCase
import com.splitit.domain.usecase.DeleteExpenseUseCase
import com.splitit.domain.usecase.DeleteSessionUseCase
import com.splitit.domain.usecase.GenerateSettlementUseCase
import com.splitit.domain.usecase.GetSettingsUseCase
import com.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.splitit.domain.usecase.ObserveSessionsUseCase
import com.splitit.domain.usecase.RemoveParticipantUseCase
import com.splitit.domain.usecase.SaveSettingsUseCase
import com.splitit.domain.usecase.UpdateExpenseUseCase
import com.splitit.domain.usecase.UpdateParticipantUseCase
import com.splitit.domain.usecase.UpdateSessionUseCase
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
    single { DeviceLocale() }
    single { DefaultLocalizationService(get()) }
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

    factory { CreateSessionUseCase(get(), get(), get()) }
    factory { UpdateSessionUseCase(get(), get()) }
    factory { DeleteSessionUseCase(get()) }
    factory { ObserveSessionsUseCase(get()) }
    factory { ObserveSessionDetailsUseCase(get(), get(), get(), get()) }

    factory { AddParticipantUseCase(get(), get(), get(), get()) }
    factory { UpdateParticipantUseCase(get(), get()) }
    factory { RemoveParticipantUseCase(get()) }

    factory { CreateExpenseUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateExpenseUseCase(get(), get(), get()) }
    factory { DeleteExpenseUseCase(get()) }

    factory { CalculateSessionBalancesUseCase(get(), get(), get()) }
    factory { GenerateSettlementUseCase(get(), get(), get(), get(), get(), get(), get()) }

    factory { GetSettingsUseCase(get()) }
    factory { SaveSettingsUseCase(get()) }
}
