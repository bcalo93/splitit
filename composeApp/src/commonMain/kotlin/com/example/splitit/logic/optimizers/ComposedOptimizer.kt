package com.example.splitit.logic.optimizers

class ComposedOptimizer<T> : Optimizer<T> {
    private val optimizers: List<Optimizer<T>>

    // TODO DEPENDENCY INJECTION
    constructor(optimizers: List<Optimizer<T>>) {
        this.optimizers = optimizers
    }

    override fun optimize(elements: Set<T>): OptimizerResult<T> {
        var finished = false
        var optimized = false
        var accumulator = elements

        while(!finished) {
            val optimizations = optimizers.map {
                val currentResult = it.optimize(accumulator)

                accumulator = currentResult.elements
                currentResult
            }

            val hasOptimized = optimizations.any{ it.optimized }

            if (hasOptimized) {
                optimized = true
            }

            finished = !hasOptimized
        }

        return OptimizerResult(
            optimized,
            accumulator
        )
    }
}