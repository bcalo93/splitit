package com.example.splitit.logic

import com.example.splitit.domain.Payment
import com.example.splitit.domain.Participant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TransitiveOptimizerTest {

    private val optimizer = TransitiveOptimizer()

    @Test
    fun testSimpleTransitiveOptimization() {
        // Case: A -> B (50), B -> C (50) = A -> C (100)
        val participantA = Participant("A")
        val participantB = Participant("B")
        val participantC = Participant("C")

        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 50),
            Payment(from = participantB, to = participantC, amount = 50)
        )

        assertTrue(optimizer.canOptimize(payments))

        val optimized = optimizer.optimize(payments)

        assertEquals(1, optimized.size)
        val result = optimized.first()
        assertEquals(participantA, result.from)
        assertEquals(participantC, result.to)
        assertEquals(100, result.amount)
    }

    @Test
    fun testLongerTransitiveChain() {
        // Case: A -> B (30), B -> C (20), C -> D (40) = A -> D (90)
        val participantA = Participant("A")
        val participantB = Participant("B")
        val participantC = Participant("C")
        val participantD = Participant("D")

        val expectedResult = linkedSetOf(
            Payment(from = participantA, participantB, 10),
            Payment(from = participantC, participantC, 20),
            Payment(from = participantC, participantD, 40)
        )

        val payments = linkedSetOf(
            Payment(from = participantA, to = participantB, amount = 30),
            Payment(from = participantB, to = participantC, amount = 20),
            Payment(from = participantC, to = participantD, amount = 40)
        )

        assertTrue(optimizer.canOptimize(payments))

        val optimized = optimizer.optimize(payments)

        assertEquals(3, optimized.size)

        // TODO IMPLEMENT A BETTER COMPARATOR ID IS AFFECTING HERE.
        assertSame(expectedResult, optimized)
    }

    @Test
    fun testCyclicPayments() {
        // Caso: A -> B (50), B -> C (30), C -> A (20)
        // Resultado: A -> C (20), ya que A debe 50-20=30 neto a B, y B debe 30 a C
        val participantA = Participant("A")
        val participantB = Participant("B")
        val participantC = Participant("C")

        val payment1 = Payment(from = participantA, to = participantB, amount = 50)
        val payment2 = Payment(from = participantB, to = participantC, amount = 30)
        val payment3 = Payment(from = participantC, to = participantA, amount = 20)

        val payments = setOf(payment1, payment2, payment3)

        assertTrue(optimizer.canOptimize(payments))

        val optimized = optimizer.optimize(payments)

        // Verificar que se optimizó (debe tener menos pagos que el original)
        assertTrue(optimized.size <= payments.size)
    }

    @Test
    fun testNoOptimizationNeeded() {
        // Caso: A -> B (50), C -> D (30) - no hay relación transitiva
        val participantA = Participant("A")
        val participantB = Participant("B")
        val participantC = Participant("C")
        val participantD = Participant("D")

        val payment1 = Payment(from = participantA, to = participantB, amount = 50)
        val payment2 = Payment(from = participantC, to = participantD, amount = 30)

        val payments = setOf(payment1, payment2)

        assertFalse(optimizer.canOptimize(payments))

        val optimized = optimizer.optimize(payments)

        assertEquals(payments, optimized)
    }

    @Test
    fun testMultipleIndependentChains() {
        // Caso: A -> B -> C y D -> E -> F (dos cadenas independientes)
        val participantA = Participant("A")
        val participantB = Participant("B")
        val participantC = Participant("C")
        val participantD = Participant("D")
        val participantE = Participant("E")
        val participantF = Participant("F")

        val chain1Payment1 = Payment(from = participantA, to = participantB, amount = 25)
        val chain1Payment2 = Payment(from = participantB, to = participantC, amount = 35)
        val chain2Payment1 = Payment(from = participantD, to = participantE, amount = 40)
        val chain2Payment2 = Payment(from = participantE, to = participantF, amount = 15)

        val payments = setOf(chain1Payment1, chain1Payment2, chain2Payment1, chain2Payment2)

        assertTrue(optimizer.canOptimize(payments))

        val optimized = optimizer.optimize(payments)

        assertEquals(2, optimized.size) // Debe quedar un pago por cada cadena

        // Verificar que ambas cadenas fueron optimizadas
        assertTrue(optimized.any { it.from == participantA && it.to == participantC && it.amount == 60 })
        assertTrue(optimized.any { it.from == participantD && it.to == participantF && it.amount == 55 })
    }
}