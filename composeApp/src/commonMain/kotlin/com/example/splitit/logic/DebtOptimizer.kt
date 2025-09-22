package com.example.splitit.logic

import com.example.splitit.domain.Participant

// Figure out if it is relevant to give it an identifier to improve optimization desition.
class DebtOptimizer {
    var optimizers: List<Optimizer<Participant>>

    constructor(optimizers: List<Optimizer<Participant>>) {
        this.optimizers = optimizers
    }

    fun optimize(participant: Set<Participant>) {
        TODO("Not yet implemented")
    }
}