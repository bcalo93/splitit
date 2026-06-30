package com.example.splitit.di

import com.example.splitit.data.database.DatabaseDriverFactory

fun appModules(databaseDriverFactory: DatabaseDriverFactory) = listOf(
    dataModule(databaseDriverFactory),
    domainModule,
    presentationModule,
)
