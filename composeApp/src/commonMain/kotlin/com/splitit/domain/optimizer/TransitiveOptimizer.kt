package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt
import com.splitit.domain.value.ParticipantId

class TransitiveOptimizer : DebtOptimizer() {
    override fun collectChunk(debts: Set<Debt>): Set<Debt> {
        val chain = LinkedHashSet<Debt>()
        var currentDebt: Debt? = debts.first()
        val visitedParticipants = mutableSetOf<ParticipantId>()

        while (currentDebt != null) {
            val added = visitedParticipants.add(currentDebt.fromParticipantId)
            if (!added) {
                break
            }

            chain.add(currentDebt)

            if (chain.size == CHUNK_SIZE) {
                break
            }

            currentDebt = debts.firstOrNull { debt ->
                val current = currentDebt
                debt != current &&
                    debt.fromParticipantId == current.toParticipantId &&
                    debt.toParticipantId != current.fromParticipantId && // Ignore cycles
                    !chain.contains(debt)
            }
        }

        return chain
    }

    override fun optimizeChunk(chunk: Set<Debt>): Set<Debt> {
        if (chunk.size < CHUNK_SIZE) return chunk

        val firstDebt = chunk.first()
        val lastDebt = chunk.last()

        val first = firstDebt.fromParticipantId
        val second = firstDebt.toParticipantId
        val last = lastDebt.toParticipantId

        return when {
            lastDebt.amount > firstDebt.amount -> setOf(
                firstDebt.copy(toParticipantId = last),
                Debt(second, last, lastDebt.amount - firstDebt.amount)
            )
            lastDebt.amount < firstDebt.amount -> setOf(
                Debt(first, second, firstDebt.amount - lastDebt.amount),
                lastDebt.copy(fromParticipantId = first)
            )
            else -> setOf(
                firstDebt.copy(toParticipantId = last)
            )
        }
    }
}
