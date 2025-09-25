package com.example.splitit.logic.optimizers

import com.example.splitit.domain.Participant
import com.example.splitit.domain.Payment
import com.example.splitit.testutils.collectionsAreEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransitiveOptimizerTest {

    private val participantA = Participant("A")
    private val participantB = Participant("B")
    private val participantC = Participant("C")
    private val participantD = Participant("D")
    private val participantE = Participant("E")
    private val participantF = Participant("F")

    private val optimizer = TransitiveOptimizer()

    @Test
    fun testSimpleTransitiveOptimizationFirstCase() {
        // Case: A -> B (50), B -> C (50) = A -> C (50)
        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 50),
            Payment(from = participantB, to = participantC, amount = 50)
        )
        val expectedResult = setOf(
            Payment(from = participantA, to = participantC, amount = 50)
        )

        val result = optimizer.optimize(payments)

        assertTrue(result.optimized)
        assertTrue(collectionsAreEquals(
            expectedResult,
            result.elements,
            paymentsAreEquals)
        )
    }

    @Test
    fun testSimpleTransitiveOptimizationSecondCase() {
        // Case: A -> B (50), B -> C (100) = A -> C (50), B -> C (50)
        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 50),
            Payment(from = participantB, to = participantC, amount = 110)
        )
        val expectedResult = setOf(
            Payment(from = participantA, to = participantC, amount = 50),
            Payment(from = participantB, to = participantC, amount = 60)
        )

        val result = optimizer.optimize(payments)
        assertTrue(result.optimized)

        assertTrue(collectionsAreEquals(expectedResult, result.elements, paymentsAreEquals))
    }

    @Test
    fun testSimpleTransitiveOptimizationThirdCase() {
        // Case: A -> B (50), B -> C (100) = A -> C (50), B -> C (50)
        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 110),
            Payment(from = participantB, to = participantC, amount = 50)
        )
        val expectedResult = setOf(
            Payment(from = participantA, to = participantB, amount = 60),
            Payment(from = participantA, to = participantC, amount = 50)
        )

        val result = optimizer.optimize(payments)
        assertTrue(result.optimized)

        assertTrue(collectionsAreEquals(
            expectedResult,
            result.elements,
            paymentsAreEquals)
        )
    }

    @Test
    fun testLongerTransitiveChain() {
        // Case: A -> B (30), B -> C (20), C -> D (40) = A -> B (10), A -> C (20), C -> D (40)
        val payments = linkedSetOf(
            Payment(from = participantA, to = participantB, amount = 30),
            Payment(from = participantB, to = participantC, amount = 20),
            Payment(from = participantC, to = participantD, amount = 40)
        )
        val expectedResult = linkedSetOf(
            Payment(from = participantA, participantB, 10),
            Payment(from = participantA, participantC, 20),
            Payment(from = participantC, participantD, 40)
        )

        val result = optimizer.optimize(payments)
        assertTrue(result.optimized)

        assertTrue(collectionsAreEquals(expectedResult, result.elements, paymentsAreEquals))
    }

    @Test
    fun testCyclicPayments() {
        // Case: A -> B (50), B -> A (30)
        // Result: Should be ignored and return same Set
        val participantA = Participant("A")
        val participantB = Participant("B")

        val payment1 = Payment(from = participantA, to = participantB, amount = 50)
        val payment2 = Payment(from = participantB, to = participantA, amount = 30)

        val payments = setOf(payment1, payment2)

        val result = optimizer.optimize(payments)

        assertFalse(result.optimized)
        assertEquals(payments, result.elements)
    }

    @Test
    fun testNoOptimizationNeeded() {
        // Case: A -> B (50), C -> D (30) - no transitive
        val payment1 = Payment(from = participantA, to = participantB, amount = 50)
        val payment2 = Payment(from = participantC, to = participantD, amount = 30)

        val payments = setOf(payment1, payment2)

        val result = optimizer.optimize(payments)

        assertFalse(result.optimized)
        assertEquals(payments, result.elements)
    }

    @Test
    fun testMultipleIndependentChains() {
        // Case: A -> B -> C y D -> E -> F (two independent chains)
        val chain1Payment1 = Payment(from = participantA, to = participantB, amount = 25)
        val chain1Payment2 = Payment(from = participantB, to = participantC, amount = 35)
        val chain2Payment1 = Payment(from = participantD, to = participantE, amount = 40)
        val chain2Payment2 = Payment(from = participantE, to = participantF, amount = 15)

        val payments = setOf(chain1Payment1, chain1Payment2, chain2Payment1, chain2Payment2)

        val expectedResult = setOf(
            Payment(from = participantA, to = participantC, amount = 25),
            Payment(from = participantB, to = participantC, amount = 10),
            Payment(from = participantD, to = participantE, amount = 25),
            Payment(from = participantD, to = participantF, amount = 15)
        )

        val result = optimizer.optimize(payments)
        assertTrue(result.optimized)

        assertTrue(collectionsAreEquals(expectedResult, result.elements, paymentsAreEquals))
    }

    @Test
    fun testEmptySet() {
        // Case: empty Set
        val payments = emptySet<Payment>()

        val result = optimizer.optimize(payments)

        assertFalse(result.optimized, "Should not have optimized (empty set)")
        assertTrue(result.elements.isEmpty(), "Should return empty set")
    }
}