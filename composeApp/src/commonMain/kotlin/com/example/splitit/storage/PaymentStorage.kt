package com.example.splitit.storage

import com.example.splitit.domain.Participant
import com.example.splitit.domain.Payment
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class PaymentStorage {
    private val payments: MutableSet<Payment> = HashSet()
    val participants: MutableSet<Participant> = HashSet()

    fun getPayments(): Set<Payment> {
        return payments
    }

    fun addPayment(payment: Payment) {
        payments.add(payment)
    }

    fun removePayment(id: Uuid): Boolean {
        return payments.removeAll { it.id == id }
    }

    fun removePayment(payment: Payment): Boolean {
        return payments.remove(payment)
    }

}