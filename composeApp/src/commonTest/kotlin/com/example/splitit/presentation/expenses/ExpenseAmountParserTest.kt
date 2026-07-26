package com.example.splitit.presentation.expenses

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpenseAmountParserTest {
    @Test
    fun parseWholeAndDecimalAmountsToMinorUnits() {
        assertEquals(1200, parseAmount("12"))
        assertEquals(1230, parseAmount("12.3"))
        assertEquals(1234, parseAmount("12.34"))
    }

    @Test
    fun rejectInvalidAmounts() {
        assertNull(parseAmount(""))
        assertNull(parseAmount("12.345"))
        assertNull(parseAmount("-1"))
        assertNull(parseAmount("abc"))
    }
}
