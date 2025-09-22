package com.example.splitit.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ParticipantTest {

    @Test
    fun createParticipant() {
        val participant = Participant("A")

        assertEquals("A", participant.nickname)
        assertTrue(participant.debts.isEmpty())
    }

    @Test
    fun equalsPayment() {
        val first = Participant("A")
        val second = Participant("A")

        assertEquals(first, second)
    }

    @Test
    fun notEqualsPayment() {
        val first = Participant("A")
        val second = Participant("B")

        assertNotEquals(first, second)
    }

    @Test
    fun hashCodePayment() {
        val participant = Participant("A")

        assertEquals(participant.nickname.hashCode(), participant.hashCode())
    }
}