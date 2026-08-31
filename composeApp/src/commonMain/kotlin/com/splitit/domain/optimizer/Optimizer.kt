package com.splitit.domain.optimizer

interface Optimizer<T> {
    fun optimize(elements: Set<T>): OptimizerResult<T>
}
