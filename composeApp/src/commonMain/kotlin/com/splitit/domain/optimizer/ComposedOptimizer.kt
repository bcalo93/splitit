package com.splitit.domain.optimizer

class ComposedOptimizer<T>(private val optimizers: List<Optimizer<T>>) : Optimizer<T> {

    override fun optimize(elements: Set<T>): OptimizerResult<T> {
        var finished = false
        var optimized = false
        var accumulator = elements

        while (!finished) {
            val optimizations = optimizers.map {
                val currentResult = it.optimize(accumulator)

                accumulator = currentResult.elements
                currentResult
            }

            val hasOptimized = optimizations.any { it.optimized }

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
