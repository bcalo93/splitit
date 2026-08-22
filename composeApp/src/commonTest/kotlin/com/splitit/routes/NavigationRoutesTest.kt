package com.splitit.routes

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationRoutesTest {

    private val json = Json

    @Test
    fun groupDetailsRouteRoundTrips() {
        val route = GroupDetails(groupId = "group-1")

        val decoded = json.decodeFromString<GroupDetails>(json.encodeToString(route))

        assertEquals("group-1", decoded.groupId)
    }

    @Test
    fun groupFormCreateRouteDefaultsToNullGroupId() {
        val route = GroupForm()

        val decoded = json.decodeFromString<GroupForm>(json.encodeToString(route))

        assertEquals(null, decoded.groupId)
    }

    @Test
    fun groupFormEditRouteRoundTrips() {
        val route = GroupForm(groupId = "group-1")

        val decoded = json.decodeFromString<GroupForm>(json.encodeToString(route))

        assertEquals("group-1", decoded.groupId)
    }

    @Test
    fun expensesRouteDefaultsToClosedForm() {
        val route = Expenses(groupId = "group-1")

        val decoded = json.decodeFromString<Expenses>(json.encodeToString(route))

        assertEquals("group-1", decoded.groupId)
        assertEquals(false, decoded.openExpenseForm)
    }

    @Test
    fun expensesRouteCarriesOpenExpenseFormFlag() {
        val route = Expenses(groupId = "group-1", openExpenseForm = true)

        val decoded = json.decodeFromString<Expenses>(json.encodeToString(route))

        assertEquals("group-1", decoded.groupId)
        assertEquals(true, decoded.openExpenseForm)
    }
}
