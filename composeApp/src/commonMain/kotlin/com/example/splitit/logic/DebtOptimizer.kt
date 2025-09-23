package com.example.splitit.logic

import com.example.splitit.domain.Participant
import com.example.splitit.domain.Payment
import com.example.splitit.storage.PaymentStorage

// Figure out if it is relevant to give it an identifier to improve optimization desition.
class DebtOptimizer {
    val optimizers: List<Optimizer<Payment>>
    val paymentStorage: PaymentStorage


    // TODO DEPENDENCY INJECTION
    constructor(optimizers: List<Optimizer<Payment>>, paymentStorage: PaymentStorage) {
        this.optimizers = optimizers
        this.paymentStorage = paymentStorage
    }

    fun optimize() {
        TODO("Not yet implemented")
    }
}