package com.example.splitit.testutils

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