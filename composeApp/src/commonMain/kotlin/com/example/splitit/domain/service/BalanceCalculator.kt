package com.example.splitit.domain.service

import com.example.splitit.domain.model.Balance
import com.example.splitit.domain.model.Debt
import com.example.splitit.domain.model.Expense
import com.example.splitit.domain.model.Participant
import com.example.splitit.domain.value.Money
import com.example.splitit.domain.value.ParticipantId

class BalanceCalculator {
    fun calculateBalances(
        participants: List<Participant>,
        expenses: List<Expense>,
    ): List<Balance> {
        val balances = participants.associate { participant ->
            participant.id to Money.zero(defaultCurrency(expenses))
        }.toMutableMap()

        expenses.forEach { expense ->
            require(balances.containsKey(expense.payerId)) {
                "Expense payer must belong to the session participants."
            }

            val shares = splitExpense(expense)
            balances[expense.payerId] = balances.getValue(expense.payerId) + expense.amount

            shares.forEach { (participantId, share) ->
                require(balances.containsKey(participantId)) {
                    "Expense participant must belong to the session participants."
                }
                balances[participantId] = balances.getValue(participantId) - share
            }
        }

        return balances.map { (participantId, amount) ->
            Balance(participantId = participantId, amount = amount)
        }
    }

    fun calculateDebts(balances: List<Balance>): List<Debt> {
        val debtors = balances
            .filter { it.amount.minorUnits < 0 }
            .map { it.participantId to -it.amount }
            .toMutableList()
        val creditors = balances
            .filter { it.amount.minorUnits > 0 }
            .map { it.participantId to it.amount }
            .toMutableList()

        val debts = mutableListOf<Debt>()
        var debtorIndex = 0
        var creditorIndex = 0

        while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
            val (debtorId, debtorAmount) = debtors[debtorIndex]
            val (creditorId, creditorAmount) = creditors[creditorIndex]
            val amount = if (debtorAmount <= creditorAmount) debtorAmount else creditorAmount

            debts += Debt(
                fromParticipantId = debtorId,
                toParticipantId = creditorId,
                amount = amount,
            )

            debtors[debtorIndex] = debtorId to (debtorAmount - amount)
            creditors[creditorIndex] = creditorId to (creditorAmount - amount)

            if (debtors[debtorIndex].second.isZero()) debtorIndex++
            if (creditors[creditorIndex].second.isZero()) creditorIndex++
        }

        return debts
    }

    private fun splitExpense(expense: Expense): Map<ParticipantId, Money> {
        val totalWeight = expense.participantShares.sumOf { it.shareWeight }
        var allocated = 0L

        return expense.participantShares
            .sortedBy { it.participantId.value }
            .mapIndexed { index, share ->
                val isLast = index == expense.participantShares.lastIndex
                val minorUnits = if (isLast) {
                    expense.amount.minorUnits - allocated
                } else {
                    (expense.amount.minorUnits * share.shareWeight) / totalWeight
                }
                allocated += minorUnits
                share.participantId to Money(minorUnits, expense.amount.currencyCode)
            }
            .toMap()
    }

    private fun defaultCurrency(expenses: List<Expense>): String {
        return expenses.firstOrNull()?.amount?.currencyCode ?: "USD"
    }
}
