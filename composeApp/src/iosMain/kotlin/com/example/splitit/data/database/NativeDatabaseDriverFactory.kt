package com.example.splitit.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class NativeDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = SplitItDatabase.Schema,
            name = DATABASE_NAME,
        )
    }

    private companion object {
        const val DATABASE_NAME = "splitit.db"
    }
}
