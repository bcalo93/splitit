package com.splitit.presentation.expenses

import com.splitit.testutils.TestIds
import com.splitit.testutils.expense
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpenseGroupingTest {
    @Test
    fun groupsExpensesByDayInDescendingOrder() {
        val dayOne = 86_400_000L * 10
        val dayTwo = 86_400_000L * 11
        val dayThree = 86_400_000L * 12

        val a = expense(id = TestIds.expense, title = "A", dateMillis = dayOne)
        val b = expense(id = TestIds.secondExpense, title = "B", dateMillis = dayTwo)
        val c = expense(title = "C", dateMillis = dayTwo + 5_000)
        val d = expense(title = "D", dateMillis = dayThree)

        val groups = groupExpensesByDay(listOf(a, b, c, d))

        assertEquals(3, groups.size)
        assertEquals(dayThree, groups[0].dayMillis)
        assertEquals(listOf("D"), groups[0].expenses.map { it.title })
        assertEquals(dayTwo, groups[1].dayMillis)
        assertEquals(listOf("C", "B"), groups[1].expenses.map { it.title })
        assertEquals(dayOne, groups[2].dayMillis)
        assertEquals(listOf("A"), groups[2].expenses.map { it.title })
    }

    @Test
    fun startOfDayFloorsToDayBoundary() {
        assertEquals(0L, startOfDay(0L))
        assertEquals(86_400_000L, startOfDay(86_400_000L + 1L))
        assertEquals(-86_400_000L, startOfDay(-1L))
    }

    @Test
    fun civilDateComputesKnownDates() {
        assertEquals(Triple(1970L, 1, 1), civilDate(0))
        assertEquals(Triple(1970L, 1, 2), civilDate(1))
        assertEquals(Triple(1970L, 2, 1), civilDate(31))
        assertEquals(Triple(2000L, 1, 1), civilDate(10_957))
    }
}
