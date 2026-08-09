package com.example.splitit.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class NativeDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(
            schema = SplitItDatabase.Schema,
            name = DATABASE_NAME,
        )
        driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        return driver
    }

    private companion object {
        const val DATABASE_NAME = "splitit.db"
    }
}
