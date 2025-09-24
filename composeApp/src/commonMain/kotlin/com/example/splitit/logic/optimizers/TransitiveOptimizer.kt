package com.example.splitit.logic.optimizers

import com.example.splitit.domain.Participant
import com.example.splitit.domain.Payment
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi

class TransitiveOptimizer: Optimizer<Payment> {

    // THIS METHOD IS DEPRECATED, I CAN COMPARE RESULT WITH ORIGINAL
    @OptIn(ExperimentalUuidApi::class)
    override fun canOptimize(elements: Set<Payment>): Boolean {
        return elements.any { payment ->
            collectChunk(elements, payment).size == CHUNK_SIZE
        }
    }

    override fun optimize(elements: Set<Payment>): Set<Payment>  {
        val chunks = createChunks(elements)

        return chunks.map{ optimizeChunk(it)}.flatten().toSet()

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
        val chain = LinkedHashSet<Payment>()
        var currentPayment: Payment? = start ?: payments.first()
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

    private fun optimizeChunk(chunk: Set<Payment>): Set<Payment> {
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

private const val CHUNK_SIZE = 2