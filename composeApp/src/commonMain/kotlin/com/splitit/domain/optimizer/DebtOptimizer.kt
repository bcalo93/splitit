package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt

abstract class DebtOptimizer : Optimizer<Debt> {
    override fun optimize(elements: Set<Debt>): OptimizerResult<Debt> {
        val chunks = createChunks(elements)

        val canOptimize = chunks.any { it.size == CHUNK_SIZE }

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

    private fun createChunks(elements: Set<Debt>): List<Set<Debt>> {
        val result = mutableListOf<Set<Debt>>()
        val debtsToProcess = elements.toMutableSet()
        while (debtsToProcess.isNotEmpty()) {
            val chunk = collectChunk(debtsToProcess)
            result.add(chunk)
            debtsToProcess.removeAll(chunk)
        }

        return result
    }

    protected abstract fun optimizeChunk(chunk: Set<Debt>): Set<Debt>
    protected abstract fun collectChunk(debts: Set<Debt>): Set<Debt>

    companion object {
        protected const val CHUNK_SIZE = 2
    }
}
