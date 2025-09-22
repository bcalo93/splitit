package com.example.splitit.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Payment {
    var id: Uuid = Uuid.random()
        private set
    var from: Participant // who has to pay
        private set
    var to: Participant // who is receiving the money
        private set
    var amount: Int
        private set

    constructor(from: Participant, to: Participant, amount: Int) {
        this.from = from
        this.to = to
        this.amount = amount
        this.from.debts.add(this)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Payment) {
            return false
        }
        return id === other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}