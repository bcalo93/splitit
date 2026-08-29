package com.splitit.domain.service

import com.splitit.domain.model.Balance
import com.splitit.domain.model.Debt
import com.splitit.domain.model.Expense
import com.splitit.domain.model.Participant
import com.splitit.domain.repository.DefaultCurrencyCode
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId

class BalanceCalculator {
    fun calculateBalances(
        participants: List<Participant>,
        expenses: List<Expense>,
    ): List<Balance> {
        require(participants.map { it.id }.toSet().size == participants.size) {
            "Group participants cannot contain duplicate IDs."
        }

        val currencyCode = defaultCurrency(expenses)
        require(expenses.all { it.amount.currencyCode == currencyCode }) {
            "All expenses in a group must use the same currency."
        }

        val balances = participants.associate { participant ->
            participant.id to Money.zero(currencyCode)
        }.toMutableMap()

        expenses.forEach { expense ->
            require(balances.containsKey(expense.payerId)) {
                "Expense payer must belong to the group participants."
            }

            val shares = splitExpense(expense)
            balances[expense.payerId] = balances.getValue(expense.payerId) + expense.amount

            shares.forEach { (participantId, share) ->
                require(balances.containsKey(participantId)) {
                    "Expense participant must belong to the group participants."
                }
                balances[participantId] = balances.getValue(participantId) - share
            }
        }

        return balances.map { (participantId, amount) ->
            Balance(participantId = participantId, amount = amount)
        }
    }

    fun calculateDebts(balances: List<Balance>): List<Debt> {
        if (balances.isEmpty()) return emptyList()

        val currencyCode = balances.first().amount.currencyCode
        require(balances.all { it.amount.currencyCode == currencyCode }) {
            "All balances must use the same currency."
        }
        val total = balances.fold(Money.zero(currencyCode)) { sum, balance ->
            sum + balance.amount
        }
        require(total.isZero()) { "Balances must sum to zero before debts are calculated." }

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
        val shares = expense.participantShares
        val totalAmount = expense.amount.minorUnits
        val sharesTotal = shares.sumOf { it.amountMinorUnits }

        require(sharesTotal == totalAmount) {
            "Share amounts ($sharesTotal) must equal the expense total ($totalAmount)."
        }

        return shares.associate { share ->
            share.participantId to Money(
                minorUnits = share.amountMinorUnits,
                currencyCode = expense.amount.currencyCode,
            )
        }
    }

    private fun defaultCurrency(expenses: List<Expense>): String {
        return expenses.firstOrNull()?.amount?.currencyCode ?: DefaultCurrencyCode
    }
}
