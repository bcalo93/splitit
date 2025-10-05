package com.example.splitit.logic.optimizers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposedOptimizerTest {

    val underTen= object : Optimizer<Int> {
        override fun optimize(elements: Set<Int>): OptimizerResult<Int> {
            val result = elements.filter { it < 10 }.toSet()
            return OptimizerResult(
                elements.size > result.size,
                result
            )
        }
    }

    val overThree = object : Optimizer<Int> {
        override fun optimize(elements: Set<Int>): OptimizerResult<Int> {
            val result = elements.filter { it > 3 }.toSet()
            return OptimizerResult(
                elements.size > result.size,
                result
            )
        }
    }

    @Test
    fun testOptimize() {
        val composedOptimizer = ComposedOptimizer(listOf(underTen, overThree))

        val result = composedOptimizer.optimize(setOf(1,2, 3, 4, 5, 10))
        assertTrue(result.optimized)
        assertEquals(setOf(4, 5), result.elements)
    }

    @Test
    fun testOptimizeTwice() {
        val composedOptimizer = ComposedOptimizer(listOf(underTen, overThree))

        val result = composedOptimizer.optimize(setOf(1,2, 3, 4, 5, 10))
        assertTrue(result.optimized)

        val result2 = composedOptimizer.optimize(result.elements)
        assertFalse(result2.optimized)
        assertEquals(setOf(4, 5), result2.elements)
    }

    @Test
    fun testNoOptimize() {
        val composedOptimizer = ComposedOptimizer(listOf(underTen, overThree))

        val result = composedOptimizer.optimize(setOf(5, 6, 7, 8, 9))
        assertFalse(result.optimized)
        assertEquals(setOf(5, 6, 7, 8, 9), result.elements)
    }

    @Test
    fun testEmptyList() {
        val composedOptimizer = ComposedOptimizer(listOf(underTen, overThree))

        val result = composedOptimizer.optimize(setOf())
        assertFalse(result.optimized)
        assertTrue(result.elements.isEmpty())
    }

    @Test
    fun testEmptyOptimizers() {
        val composedOptimizer = ComposedOptimizer(listOf<Optimizer<Int>>())

        val result = composedOptimizer.optimize(setOf(1,2, 3, 4, 5, 10))
        assertFalse(result.optimized)
        assertEquals(setOf(1,2, 3, 4, 5, 10), result.elements)

    }
}