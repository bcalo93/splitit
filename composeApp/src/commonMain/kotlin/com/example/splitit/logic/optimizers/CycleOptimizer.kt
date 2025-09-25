package com.example.splitit.logic.optimizers

import com.example.splitit.domain.Participant
import com.example.splitit.domain.Payment
import kotlin.collections.iterator
import kotlin.math.abs

class CycleOptimizer : Optimizer<Payment> {
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

    private fun collectChunk(payments: Set<Payment>, start: Payment? = null): Set<Payment> {
        val currentPayment: Payment = start ?: payments.first()

        val cycle = payments.firstOrNull {
            it != currentPayment
                && it.to == currentPayment.from
                && it.from == currentPayment.to
        }

        return if (cycle != null) setOf(currentPayment, cycle) else setOf(currentPayment)
    }

    private fun optimizeChunk(chunk: Set<Payment>): Set<Payment> {
        if (chunk.size < CHUNK_SIZE) {
            return chunk
        }

        val first = chunk.first()
        val second = chunk.last()
        val diff = first.amount - second.amount

        if (diff > 0) {
            return setOf(
                Payment(from = first.from, to = first.to, amount = diff)
            )
        }

        if (diff < 0) {
            return setOf(
                Payment(from = second.from, to = second.to, amount = abs(diff))
            )
        }

        return HashSet()
    }
}

private const val CHUNK_SIZE = 2