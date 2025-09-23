package com.example.splitit.logic

import com.example.splitit.domain.Participant
import com.example.splitit.domain.Payment

class CycleOptimizer : Optimizer<Payment> {
    override fun canOptimize(elements: Set<Payment>): Boolean {
        TODO("Not yet implemented")
    }

    override fun optimize(elements: Set<Payment>): Set<Payment> {
        TODO("Not yet implemented")
    }

    private fun optimizeCycle(chain: List<Payment>): List<Payment> {
        // En un ciclo, podemos calcular el balance neto de cada participante
        val balances = mutableMapOf<Participant, Int>()

        chain.forEach { payment ->
            balances[payment.from] = (balances[payment.from] ?: 0) - payment.amount
            balances[payment.to] = (balances[payment.to] ?: 0) + payment.amount
        }

        // Crear pagos solo para balances no nulos
        val result = mutableListOf<Payment>()
        val debtors = balances.filter { it.value < 0 }
        val creditors = balances.filter { it.value > 0 }

        for ((debtor, debtAmount) in debtors) {
            var remainingDebt = -debtAmount
            for ((creditor, creditAmount) in creditors) {
                if (remainingDebt <= 0) break

                val paymentAmount = minOf(remainingDebt, creditAmount)
                if (paymentAmount > 0) {
                    result.add(Payment(from = debtor, to = creditor, amount = paymentAmount))
                    remainingDebt -= paymentAmount
                }
            }
        }

        return result
    }
}