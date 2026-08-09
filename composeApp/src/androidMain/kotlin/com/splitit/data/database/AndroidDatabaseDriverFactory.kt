package com.splitit.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabaseDriverFactory(
    private val context: Context,
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(
            schema = SplitItDatabase.Schema,
            context = context,
            name = DATABASE_NAME,
        )
        driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        return driver
    }

    private companion object {
        const val DATABASE_NAME = "splitit.db"
    }
}
