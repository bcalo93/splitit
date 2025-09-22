package com.example.splitit.domain

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class PaymentTest {
    val from = Participant("A")
    val to = Participant("B")
    val amount = 200

    @Test
    fun createPayment() {
        val payment = Payment(from, to, amount)

        assertEquals(payment.from, from)
        assertEquals(payment.to, to)
        assertEquals(payment.amount, amount)
        assertContains(from.debts, payment)
        assertTrue(to.debts.isEmpty())
    }

    @Test
    fun equalsPayment() {
        val payment = Payment(from, to, amount)

        assertEquals(payment, payment)
    }

    @Test
    fun notEqualsPayment() {
        val firstPayment = Payment(from, to, amount)
        val secondPayment = Payment(from, to, amount)

        assertNotEquals(firstPayment, secondPayment)
    }

    @Test
    fun hashCodePayment() {
        val payment = Payment(from, to, amount)

        assertEquals(payment.id.hashCode(), payment.hashCode())
    }
}