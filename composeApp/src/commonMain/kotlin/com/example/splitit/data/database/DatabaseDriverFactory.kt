package com.example.splitit.data.database

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DatabaseDriverFactory): SplitItDatabase {
    return SplitItDatabase(driverFactory.createDriver())
}
