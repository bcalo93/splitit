package com.example.splitit.domain

class Participant {
    var nickname: String
        private set
    var debts: MutableSet<Payment> = mutableSetOf<Payment>()
        private set

    constructor(name: String) {
        this.nickname = name
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Participant) {
            return false
        }
        return nickname === other.nickname
    }

    override fun hashCode(): Int {
        return nickname.hashCode()
    }
}