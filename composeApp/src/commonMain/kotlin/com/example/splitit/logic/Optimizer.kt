package com.example.splitit.logic

interface Optimizer<T> {
    fun canOptimize(elements: Set<T>): Boolean
    fun optimize(elements: Set<T>): Set<T>
}