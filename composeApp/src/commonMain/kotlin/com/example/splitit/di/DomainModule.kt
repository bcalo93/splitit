package com.example.splitit.di

import com.example.splitit.domain.Payment
import com.example.splitit.domain.optimizer.PaymentOptimizerAdapter
import com.example.splitit.domain.service.BalanceCalculator
import com.example.splitit.domain.usecase.AddParticipantUseCase
import com.example.splitit.domain.usecase.CalculateSessionBalancesUseCase
import com.example.splitit.domain.usecase.CreateExpenseUseCase
import com.example.splitit.domain.usecase.CreateSessionUseCase
import com.example.splitit.domain.usecase.DeleteExpenseUseCase
import com.example.splitit.domain.usecase.DeleteSessionUseCase
import com.example.splitit.domain.usecase.GenerateSettlementUseCase
import com.example.splitit.domain.usecase.GetSettingsUseCase
import com.example.splitit.domain.usecase.ObserveSessionDetailsUseCase
import com.example.splitit.domain.usecase.ObserveSessionsUseCase
import com.example.splitit.domain.usecase.RemoveParticipantUseCase
import com.example.splitit.domain.usecase.SaveSettingsUseCase
import com.example.splitit.domain.usecase.UpdateExpenseUseCase
import com.example.splitit.domain.usecase.UpdateParticipantUseCase
import com.example.splitit.domain.usecase.UpdateSessionUseCase
import com.example.splitit.domain.value.Clock
import com.example.splitit.domain.value.IdGenerator
import com.example.splitit.domain.value.SystemClock
import com.example.splitit.domain.value.UuidGenerator
import com.example.splitit.logic.optimizers.ComposedOptimizer
import com.example.splitit.logic.optimizers.Optimizer
import com.example.splitit.logic.optimizers.debt.CycleOptimizer
import com.example.splitit.logic.optimizers.debt.TransitiveOptimizer
import org.koin.dsl.module

val domainModule = module {
    single<IdGenerator> { UuidGenerator() }
    single<Clock> { SystemClock() }
    single { BalanceCalculator() }

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
