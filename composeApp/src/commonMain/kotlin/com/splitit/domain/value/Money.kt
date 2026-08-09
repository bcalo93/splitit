package com.splitit.domain.value

data class Money(
    val minorUnits: Long,
    val currencyCode: String,
) : Comparable<Money> {
    init {
        require(currencyCode.isNotBlank()) { "Currency code cannot be blank." }
    }

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits - other.minorUnits)
    }

    operator fun unaryMinus(): Money = copy(minorUnits = -minorUnits)

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return minorUnits.compareTo(other.minorUnits)
    }

    fun isZero(): Boolean = minorUnits == 0L

    fun isPositive(): Boolean = minorUnits > 0L

    private fun requireSameCurrency(other: Money) {
        require(currencyCode == other.currencyCode) {
            "Cannot operate on different currencies: $currencyCode and ${other.currencyCode}."
        }
    }

    companion object {
        fun zero(currencyCode: String): Money = Money(0, currencyCode)
    }
}
