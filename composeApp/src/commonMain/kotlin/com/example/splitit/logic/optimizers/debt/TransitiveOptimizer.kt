package com.example.splitit.logic.optimizers.debt

import com.example.splitit.domain.Participant
import com.example.splitit.domain.Payment
import kotlin.math.abs

class TransitiveOptimizer: DebtOptimizer() {
    override fun collectChunk(payments: Set<Payment>): Set<Payment> {
        val chain = LinkedHashSet<Payment>()
        var currentPayment: Payment? = payments.first()
        val visitedParticipants = mutableSetOf<Participant>()

        while (currentPayment != null) {
            val added = visitedParticipants.add(currentPayment.from)
            if (!added) {
                break
            }

            chain.add(currentPayment)

            if (chain.size == CHUNK_SIZE) {
                break
            }

            currentPayment = payments.firstOrNull { payment ->
                payment != currentPayment &&
                        payment.from == currentPayment.to &&
                        payment.to != currentPayment.from && // Ignore Cycles
                        !chain.contains(payment)
            }
        }

        return chain
    }

    override fun optimizeChunk(chunk: Set<Payment>): Set<Payment> {
        if (chunk.size < CHUNK_SIZE) return chunk

        val firstPayment = chunk.first()
        val lastPayment = chunk.last()

        val first = firstPayment.from
        val second = firstPayment.to
        val last = lastPayment.to

        val diff = lastPayment.amount - firstPayment.amount

        if (diff > 0) {
            return hashSetOf(
                Payment(first, last, firstPayment.amount),
                Payment(second, last, diff)
            )
        }

        if(diff < 0) {
            return hashSetOf(
                Payment(first, second, abs(diff)),
                Payment(first, last, lastPayment.amount )
            )
        }

        return hashSetOf(
            Payment(first, last, firstPayment.amount)
        )
    }

}
