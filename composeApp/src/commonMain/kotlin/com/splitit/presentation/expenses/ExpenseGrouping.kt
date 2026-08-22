package com.splitit.presentation.expenses

import androidx.compose.runtime.Immutable
import com.splitit.domain.model.Expense

const val MILLIS_PER_DAY = 86_400_000L

@Immutable
data class ExpenseGroup(
    val dayMillis: Long,
    val expenses: List<Expense>,
)

fun groupExpensesByDay(expenses: List<Expense>): List<ExpenseGroup> {
    return expenses
        .groupBy { startOfDay(it.dateMillis) }
        .entries
        .sortedByDescending { it.key }
        .map { (dayMillis, items) ->
            ExpenseGroup(
                dayMillis = dayMillis,
                expenses = items.sortedByDescending { it.dateMillis },
            )
        }
}

fun startOfDay(millis: Long): Long = floorDiv(millis, MILLIS_PER_DAY) * MILLIS_PER_DAY

fun civilDate(epochDay: Long): Triple<Long, Int, Int> {
    val z = epochDay + 719468
    val era = floorDiv(z, 146097L)
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
    val y = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val day = (doy - (153L * mp + 2L) / 5L + 1L).toInt()
    val month = (mp + if (mp < 10L) 3L else -9L).toInt()
    val year = y + if (month <= 2) 1 else 0
    return Triple(year, month, day)
}

private fun floorDiv(x: Long, y: Long): Long {
    val quotient = x / y
    val remainder = x % y
    return if ((x xor y) < 0L && remainder != 0L) quotient - 1 else quotient
}
