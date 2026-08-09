package com.splitit.domain.value

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MoneyTest {
    @Test
    fun addMoneyWithSameCurrency() {
        val result = Money(100, "USD") + Money(50, "USD")

        assertEquals(Money(150, "USD"), result)
    }

    @Test
    fun rejectOperationsWithDifferentCurrencies() {
        assertFailsWith<IllegalArgumentException> {
            Money(100, "USD") + Money(50, "EUR")
        }
    }

    @Test
    fun compareMoneyWithSameCurrency() {
        assertTrue(Money(150, "USD") > Money(50, "USD"))
    }
}
