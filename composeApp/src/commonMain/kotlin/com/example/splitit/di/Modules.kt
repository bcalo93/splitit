package com.example.splitit.di

import com.example.splitit.domain.Payment
import com.example.splitit.logic.optimizers.ComposedOptimizer
import com.example.splitit.logic.optimizers.Optimizer
import com.example.splitit.logic.optimizers.debt.CycleOptimizer
import com.example.splitit.logic.optimizers.debt.TransitiveOptimizer
import com.example.splitit.storage.Storage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val storageModule = module {
    singleOf(::Storage)
}

val logicModule = module {
    single { ComposedOptimizer(listOf(CycleOptimizer(), TransitiveOptimizer())) as Optimizer<Payment> }
}

val modules = listOf(storageModule, logicModule)