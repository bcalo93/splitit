package com.splitit.logic.optimizers

interface Optimizer<T> {
    fun optimize(elements: Set<T>): OptimizerResult<T>
}