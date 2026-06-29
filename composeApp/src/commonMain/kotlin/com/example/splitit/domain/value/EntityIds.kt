package com.example.splitit.domain.value

import kotlin.jvm.JvmInline

@JvmInline
value class SessionId(val value: String) {
    init {
        require(value.isNotBlank()) { "SessionId cannot be blank." }
    }
}

@JvmInline
value class ParticipantId(val value: String) {
    init {
        require(value.isNotBlank()) { "ParticipantId cannot be blank." }
    }
}

@JvmInline
value class ExpenseId(val value: String) {
    init {
        require(value.isNotBlank()) { "ExpenseId cannot be blank." }
    }
}

@JvmInline
value class SettlementId(val value: String) {
    init {
        require(value.isNotBlank()) { "SettlementId cannot be blank." }
    }
}

@JvmInline
value class TransferId(val value: String) {
    init {
        require(value.isNotBlank()) { "TransferId cannot be blank." }
    }
}
