package com.example.splitit.logic.optimizers.debt

import com.example.splitit.domain.Payment
import com.example.splitit.logic.optimizers.Optimizer
import com.example.splitit.logic.optimizers.OptimizerResult

abstract class DebtOptimizer: Optimizer<Payment> {
    override fun optimize(elements: Set<Payment>): OptimizerResult<Payment> {
        val chunks = createChunks(elements)

        val canOptimize = chunks.any{ it.size == CHUNK_SIZE }

        val result = if (canOptimize) {
            chunks.map { optimizeChunk(it) }.flatten().toSet()
        } else {
            elements
        }

        return OptimizerResult(
            optimized = canOptimize,
            elements = result
        )
    }

    private fun createChunks(elements: Set<Payment>): List<Set<Payment>> {
        val result = mutableListOf<Set<Payment>>()
        val paymentsToProcess = elements.toMutableSet()
        while (paymentsToProcess.isNotEmpty()) {
            val chunk = collectChunk(paymentsToProcess)
            result.add(chunk)
            paymentsToProcess.removeAll(chunk)
        }

        return result
    }

    protected abstract fun optimizeChunk(chunk: Set<Payment>): Set<Payment>
    protected abstract fun collectChunk(payments: Set<Payment>): Set<Payment>

    companion object {
        protected const val CHUNK_SIZE = 2
    }
}