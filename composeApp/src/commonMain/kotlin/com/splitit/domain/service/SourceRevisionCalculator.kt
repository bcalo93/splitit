package com.splitit.domain.service

import com.splitit.domain.model.Expense
import com.splitit.domain.model.Participant

/**
 * Creates a stable fingerprint for the data used to calculate a settlement.
 *
 * Timestamps alone cannot detect a deletion or two edits made in the same
 * millisecond, so the complete source data is included in the fingerprint.
 */
object SourceRevisionCalculator {
    private const val FNV_OFFSET_BASIS = -3750763034362895579L
    private const val FNV_PRIME = 1099511628211L

    fun calculate(
        participants: List<Participant>,
        expenses: List<Expense>,
    ): Long {
        val source = buildString {
            appendField(participants.size.toString())
            participants.sortedBy { it.id.value }.forEach { participant ->
                appendField("participant")
                appendField(participant.id.value)
                appendField(participant.groupId.value)
                appendField(participant.name)
                appendField(participant.avatarColor)
                appendField(participant.createdAtMillis.toString())
                appendField(participant.updatedAtMillis.toString())
            }

            appendField(expenses.size.toString())
            expenses.sortedBy { it.id.value }.forEach { expense ->
                appendField("expense")
                appendField(expense.id.value)
                appendField(expense.groupId.value)
                appendField(expense.title)
                appendField(expense.amount.minorUnits.toString())
                appendField(expense.amount.currencyCode)
                appendField(expense.payerId.value)
                appendField(expense.dateMillis.toString())
                appendField(expense.note)
                appendField(expense.createdAtMillis.toString())
                appendField(expense.updatedAtMillis.toString())
                appendField(expense.participantShares.size.toString())
                expense.participantShares
                    .sortedBy { it.participantId.value }
                    .forEach { share ->
                        appendField(share.participantId.value)
                        appendField(share.amountMinorUnits.toString())
                    }
            }
        }

        var hash = FNV_OFFSET_BASIS
        source.forEach { character ->
            hash = (hash xor character.code.toLong()) * FNV_PRIME
        }

        return hash and Long.MAX_VALUE
    }

    private fun StringBuilder.appendField(value: String?) {
        if (value == null) {
            append("-1:")
        } else {
            append(value.length)
            append(':')
            append(value)
        }
        append('|')
    }
}
