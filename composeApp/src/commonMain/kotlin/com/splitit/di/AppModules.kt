package com.splitit.di

import com.splitit.data.database.DatabaseDriverFactory

fun appModules(databaseDriverFactory: DatabaseDriverFactory) = listOf(
    dataModule(databaseDriverFactory),
    domainModule,
    presentationModule,
)
