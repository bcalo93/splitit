package com.splitit.domain.optimizer

import com.splitit.domain.model.Debt
import com.splitit.domain.value.Money
import com.splitit.domain.value.ParticipantId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CycleOptimizerTest {

    private val optimizer = CycleOptimizer()
    private val participantA = ParticipantId("A")
    private val participantB = ParticipantId("B")
    private val participantC = ParticipantId("C")
    private val participantD = ParticipantId("D")

    @Test
    fun testSimpleCycleOptimization_AOwesMoreToB() {
        // Case: A -> B (100), B -> A (30)
        // Expected result: A -> B (70)
        val debt1 = Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(100, "USD"))
        val debt2 = Debt(fromParticipantId = participantB, toParticipantId = participantA, amount = Money(30, "USD"))
        val debts = setOf(debt1, debt2)

        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(70, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertEquals(expectedResult, result.elements, "A owes to B 70")
    }

    @Test
    fun testSimpleCycleOptimization_BOwesMoreToA() {
        // Case: A -> B (40), B -> A (90)
        // Expected result: B -> A (50)
        val debt1 = Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(40, "USD"))
        val debt2 = Debt(fromParticipantId = participantB, toParticipantId = participantA, amount = Money(90, "USD"))
        val debts = setOf(debt1, debt2)

        val expectedResult = setOf(
            Debt(fromParticipantId = participantB, toParticipantId = participantA, amount = Money(50, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertEquals(expectedResult, result.elements, "B owes A 50")
    }

    @Test
    fun testEqualAmountsCycle_CancelOut() {
        // Case: A -> B (75), B -> A (75)
        // Expected result: empty Set
        val debt1 = Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(75, "USD"))
        val debt2 = Debt(fromParticipantId = participantB, toParticipantId = participantA, amount = Money(75, "USD"))
        val debts = setOf(debt1, debt2)

        val result = optimizer.optimize(debts)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertTrue(result.elements.isEmpty(), "Should have no payments after complete cancellation")
    }

    @Test
    fun testMultipleCycles() {
        // Case: A -> B (60), B -> A (20), C -> D (20), D -> C (70)
        // Expected result: A -> B (40), D -> C (50)
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(60, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantA, amount = Money(20, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantD, amount = Money(20, "USD")),
            Debt(fromParticipantId = participantD, toParticipantId = participantC, amount = Money(70, "USD"))
        )

        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(40, "USD")),
            Debt(fromParticipantId = participantD, toParticipantId = participantC, amount = Money(50, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertTrue(result.optimized, "Should have optimized the cycles")
        assertEquals(expectedResult, result.elements, "A owes B 40 and D owes C 50")
    }

    @Test
    fun testNoCyclePresent() {
        // Case: A -> B (50), C -> D (30) - no cycles
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantD, amount = Money(30, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertFalse(result.optimized, "Should not have optimized (no cycles present)")
        assertEquals(debts, result.elements, "Should return original payments unchanged")
    }

    @Test
    fun testSinglePayment() {
        // Case: solo A -> B (100)
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(100, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertFalse(result.optimized, "Should not have optimized (no cycle possible)")
        assertEquals(debts, result.elements, "Should return original payment unchanged")
    }

    @Test
    fun testEmptySet() {
        // Case: empty Set
        val debts = emptySet<Debt>()

        val result = optimizer.optimize(debts)

        assertFalse(result.optimized, "Should not have optimized (empty set)")
        assertTrue(result.elements.isEmpty(), "Should return empty set")
    }

    @Test
    fun testThreeWayCycle_NoOptimization() {
        // Case: A -> B -> C -> A (no optimization applied)
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantC, amount = Money(40, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantA, amount = Money(30, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertFalse(result.optimized, "Should not optimize 3-way cycles (not direct cycles)")
        assertEquals(debts, result.elements, "Should return original payments unchanged")
    }

    @Test
    fun testMixedCycleAndNonCycle() {
        // Case: A -> B (60), B -> A (10), C -> D (40)
        // A-B cycle only
        val debts = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(60, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantD, amount = Money(40, "USD")),
            Debt(fromParticipantId = participantB, toParticipantId = participantA, amount = Money(10, "USD"))
        )

        val expectedResult = setOf(
            Debt(fromParticipantId = participantA, toParticipantId = participantB, amount = Money(50, "USD")),
            Debt(fromParticipantId = participantC, toParticipantId = participantD, amount = Money(40, "USD"))
        )

        val result = optimizer.optimize(debts)

        assertTrue(result.optimized, "Should have optimized the cycle")
        assertEquals(expectedResult, result.elements, "A owes B 50 and C owes D 40 (unchanged)")
    }
}
