package com.example.splitit.logic.optimizers

import com.example.splitit.domain.Payment

val paymentsAreEquals: (Payment, Payment) -> Boolean = { a, b ->
    a.to == b.to && a.from == b.from && a.amount == b.amount
}