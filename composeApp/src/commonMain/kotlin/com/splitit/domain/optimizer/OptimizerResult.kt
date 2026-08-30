package com.splitit.domain.optimizer

data class OptimizerResult<T>(val optimized: Boolean, val elements: Set<T>)
