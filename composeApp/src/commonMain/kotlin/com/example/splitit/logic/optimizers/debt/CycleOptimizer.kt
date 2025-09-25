package com.example.splitit.logic.optimizers.debt

import com.example.splitit.domain.Payment
import kotlin.math.abs

class CycleOptimizer : DebtOptimizer() {
    override fun collectChunk(payments: Set<Payment>): Set<Payment> {
        val current = payments.first()

        val cycle = payments.firstOrNull {
            it != current
                && it.to == current.from
                && it.from == current.to
        }

        return if (cycle != null) setOf(current, cycle) else setOf(current)
    }

    override fun optimizeChunk(chunk: Set<Payment>): Set<Payment> {
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