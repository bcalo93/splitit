package com.example.splitit.logic.optimizers.debt

import com.example.splitit.domain.Payment
import com.example.splitit.domain.Participant
import com.example.splitit.testutils.collectionsAreEquals
import com.example.splitit.testutils.paymentsAreEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CycleOptimizerTest {

    private val optimizer = CycleOptimizer()
    private val participantA = Participant("A")
    private val participantB = Participant("B")
    private val participantC = Participant("C")
    private val participantD = Participant("D")

    @Test
    fun testSimpleCycleOptimization_AOwesMoreToB() {
        // Case: A -> B (100), B -> A (30)
        // Expected result: A -> B (70)
        val payment1 = Payment(from = participantA, to = participantB, amount = 100)
        val payment2 = Payment(from = participantB, to = participantA, amount = 30)
        val payments = setOf(payment1, payment2)

        val expectedResult = Payment(
            from = participantA,
            to = participantB,
            amount = 70
        )

        val result = optimizer.optimize(payments)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertEquals(1, result.elements.size, "Should have exactly one payment after optimization")

        val optimizedPayment = result.elements.first()
        assertTrue(paymentsAreEquals(expectedResult, optimizedPayment),"A owes to B 70")
    }

    @Test
    fun testSimpleCycleOptimization_BOwesMoreToA() {
        // Case: A -> B (40), B -> A (90)
        // Expected result: B -> A (50)
        val payment1 = Payment(from = participantA, to = participantB, amount = 40)
        val payment2 = Payment(from = participantB, to = participantA, amount = 90)
        val payments = setOf(payment1, payment2)

        val expectedResult = Payment(
            from = participantB,
            to = participantA,
            amount = 50
        )

        val result = optimizer.optimize(payments)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertEquals(1, result.elements.size, "Should have exactly one payment after optimization")

        val optimizedPayment = result.elements.first()
        assertTrue(paymentsAreEquals(expectedResult, optimizedPayment),"B owes A 50")
    }

    @Test
    fun testEqualAmountsCycle_CancelOut() {
        // Case: A -> B (75), B -> A (75)
        // Expected result: empty Set
        val payment1 = Payment(from = participantA, to = participantB, amount = 75)
        val payment2 = Payment(from = participantB, to = participantA, amount = 75)
        val payments = setOf(payment1, payment2)

        val result = optimizer.optimize(payments)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertTrue(result.elements.isEmpty(), "Should have no payments after complete cancellation")
    }

    @Test
    fun testMultipleCycles() {
        // Case: A -> B (60), B -> A (20), C -> D (20), D -> C (70)
        // Expected result: A -> B (40), D -> C (50)
        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 60),
            Payment(from = participantB, to = participantA, amount = 20),
            Payment(from = participantC, to = participantD, amount = 20),
            Payment(from = participantD, to = participantC, amount = 70)
        )

        val expectedResult = setOf(
            Payment(from = participantA, to = participantB, amount = 40),
            Payment(from = participantD, to = participantC, amount = 50)
        )

        val result = optimizer.optimize(payments)

        assertTrue(result.optimized, "Should have optimized the cycles")
        assertTrue(collectionsAreEquals(
            expectedResult,
            result.elements,
            paymentsAreEquals),
            "A owns B 40 and D owns C 50"
        )
    }

    @Test
    fun testNoCyclePresent() {
        // Case: A -> B (50), C -> D (30) - no cycles
        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 50),
            Payment(from = participantC, to = participantD, amount = 30)
        )

        val result = optimizer.optimize(payments)

        assertFalse(result.optimized, "Should not have optimized (no cycles present)")
        assertEquals(payments, result.elements, "Should return original payments unchanged")
    }

    @Test
    fun testSinglePayment() {
        // Case: solo A -> B (100)
        val payment = Payment(from = participantA, to = participantB, amount = 100)
        val payments = setOf(payment)

        val result = optimizer.optimize(payments)

        assertFalse(result.optimized, "Should not have optimized (no cycle possible)")
        assertEquals(payments, result.elements, "Should return original payment unchanged")
    }

    @Test
    fun testEmptySet() {
        // Case: empty Set
        val payments = emptySet<Payment>()

        val result = optimizer.optimize(payments)

        assertFalse(result.optimized, "Should not have optimized (empty set)")
        assertTrue(result.elements.isEmpty(), "Should return empty set")
    }

    @Test
    fun testThreeWayCycle_NoOptimization() {
        // Case: A -> B -> C -> A (no optimization applied)
        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 50),
            Payment(from = participantB, to = participantC, amount = 40),
            Payment(from = participantC, to = participantA, amount = 30)
        )

        val result = optimizer.optimize(payments)

        assertFalse(result.optimized, "Should not optimize 3-way cycles (not direct cycles)")
        assertEquals(payments, result.elements, "Should return original payments unchanged")
    }

    @Test
    fun testMixedCycleAndNonCycle() {
        // Case: A -> B (60), B -> A (10), C -> D (40)
        // A-B cycle only
        val payments = setOf(
            Payment(from = participantA, to = participantB, amount = 60),
            Payment(from = participantC, to = participantD, amount = 40),
            Payment(from = participantB, to = participantA, amount = 10)
        )

        val expectedResult = setOf(
            Payment(from = participantA, to = participantB, amount = 50),
            Payment(from = participantC, to = participantD, amount = 40)
        )

        val result = optimizer.optimize(payments)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertTrue(collectionsAreEquals(
            expectedResult,
            result.elements,
            paymentsAreEquals),
            "A owns B 50 and C owns D 40 (unchanged)"
        )
    }
}