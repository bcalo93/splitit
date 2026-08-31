package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt

class CycleOptimizer : DebtOptimizer() {
    override fun collectChunk(debts: Set<Debt>): Set<Debt> {
        val current = debts.first()

        val cycle = debts.firstOrNull {
            it != current
                && it.toParticipantId == current.fromParticipantId
                && it.fromParticipantId == current.toParticipantId
        }

        return if (cycle != null) setOf(current, cycle) else setOf(current)
    }

    override fun optimizeChunk(chunk: Set<Debt>): Set<Debt> {
        if (chunk.size < CHUNK_SIZE) {
            return chunk
        }

        val first = chunk.first()
        val second = chunk.last()

        return when {
            first.amount > second.amount -> setOf(
                first.copy(amount = first.amount - second.amount)
            )
            first.amount < second.amount -> setOf(
                second.copy(amount = second.amount - first.amount)
            )
            else -> emptySet()
        }
    }
}
