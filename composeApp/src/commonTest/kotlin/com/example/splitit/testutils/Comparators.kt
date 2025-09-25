package com.example.splitit.testutils

import com.example.splitit.domain.Payment

fun <T> collectionsAreEquals(
    aCollection: Collection<T>,
    anotherCollection: Collection<T>, predicate: (T, T) -> Boolean
): Boolean {
    if (aCollection.size != anotherCollection.size) {
        return false
    }

    return aCollection.all {
        anElement -> anotherCollection.any{ anotherElement -> predicate(anElement, anotherElement) }
    }
}

val paymentsAreEquals: (Payment, Payment) -> Boolean = { a, b ->
    a.to == b.to && a.from == b.from && a.amount == b.amount
}
