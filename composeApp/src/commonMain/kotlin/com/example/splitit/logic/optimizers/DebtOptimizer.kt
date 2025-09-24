package com.example.splitit.logic.optimizers

import com.example.splitit.domain.Payment

// TODO THIS MAY BE GENERIC TOO. AS IS TOO ABSTRACT ComposedOptimizer
class DebtOptimizer : Optimizer<Payment> {
    private val optimizers: List<Optimizer<Payment>>

    // TODO DEPENDENCY INJECTION
    constructor(optimizers: List<Optimizer<Payment>>) {
        this.optimizers = optimizers
    }

    override fun optimize(elements: Set<Payment>): OptimizerResult<Payment> {
        TODO("Not yet implemented")
    }
}