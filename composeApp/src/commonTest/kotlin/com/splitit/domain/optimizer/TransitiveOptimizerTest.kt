package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransitiveOptimizerTest {

    private val participantA = ParticipantId("A")
    private val participantB = ParticipantId("B")
    private val participantC = ParticipantId("C")
    private val participantD = ParticipantId("D")
    private val participantE = ParticipantId("E")
    private val participantF = ParticipantId("F")

    private val optimizer = TransitiveOptimizer()

    @Test
    fun testSimpleTransitiveOptimizationFirstCase() {
        // Case: A -> B (50), B -> C (50) = A -> C (50)
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(50, "USD"))
        )
        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantC, amount = Money(50, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertTrue(result.optimized)
        assertEquals(expectedResult, result.elements)
    }

    @Test
    fun testSimpleTransitiveOptimizationSecondCase() {
        // Case: A -> B (50), B -> C (110) = A -> C (50), B -> C (60)
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(110, "USD"))
        )
        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantC, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(60, "USD"))
        )

        val result = optimizer.optimize(debts)
        assertTrue(result.optimized)

        assertEquals(expectedResult, result.elements)
    }

    @Test
    fun testSimpleTransitiveOptimizationThirdCase() {
        // Case: A -> B (110), B -> C (50) = A -> B (60), A -> C (50)
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(110, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(50, "USD"))
        )
        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(60, "USD")),
            Debt(fromParticipantId = participantA, toParticipantId = participantC, amount = Money(50, "USD"))
        )

        val result = optimizer.optimize(debts)
        assertTrue(result.optimized)

        assertEquals(expectedResult, result.elements)
    }

    @Test
    fun testLongerTransitiveChain() {
        // Case: A -> B (30), B -> C (20), C -> D (40) = A -> B (10), A -> C (20), C -> D (40)
        val debts = linkedSetOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(30, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(20, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantD, amount = Money(40, "USD"))
        )
        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(10, "USD")),
            Debt(fromParticipantId = participantA, toParticipantId = participantC, amount = Money(20, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantD, amount = Money(40, "USD"))
        )

        val result = optimizer.optimize(debts)
        assertTrue(result.optimized)

        assertEquals(expectedResult, result.elements)
    }

    @Test
    fun testCyclicPayments() {
        // Case: A -> B (50), B -> A (30)
        // Result: Should be ignored and return same Set
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantA, amount = Money(30, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertFalse(result.optimized)
        assertEquals(debts, result.elements)
    }

    @Test
    fun testNoOptimizationNeeded() {
        // Case: A -> B (50), C -> D (30) - no transitive
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantD, amount = Money(30, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertFalse(result.optimized)
        assertEquals(debts, result.elements)
    }

    @Test
    fun testMultipleIndependentChains() {
        // Case: A -> B -> C y D -> E -> F (two independent chains)
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(25, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(35, "USD")),
            Debt(fromParticipantId = participantD, toParticipantId = participantE, amount = Money(40, "USD")),
            Debt(fromParticipantId = participantE, toParticipantId = participantF, amount = Money(15, "USD"))
        )

        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantC, amount = Money(25, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(10, "USD")),
            Debt(fromParticipantId = participantD, toParticipantId = participantE, amount = Money(25, "USD")),
            Debt(fromParticipantId = participantD, toParticipantId = participantF, amount = Money(15, "USD"))
        )

        val result = optimizer.optimize(debts)
        assertTrue(result.optimized)

        assertEquals(expectedResult, result.elements)
    }

    @Test
    fun testEmptySet() {
        // Case: empty Set
        val debts = emptySet<Debt>()

        val result = optimizer.optimize(debts)

        assertFalse(result.optimized, "Should not have optimized (empty set)")
        assertTrue(result.elements.isEmpty(), "Should return empty set")
    }
}
