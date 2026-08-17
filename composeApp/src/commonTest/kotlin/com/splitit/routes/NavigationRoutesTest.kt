package com.splitit.routes

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationRoutesTest {

    private val json = Json

    @Test
    fun sessionDetailsRouteRoundTrips() {
        val route = SessionDetails(sessionId = "session-1")

        val decoded = json.decodeFromString<SessionDetails>(json.encodeToString(route))

        assertEquals("session-1", decoded.sessionId)
    }

    @Test
    fun sessionFormCreateRouteDefaultsToNullSessionId() {
        val route = SessionForm()

        val decoded = json.decodeFromString<SessionForm>(json.encodeToString(route))

        assertEquals(null, decoded.sessionId)
    }

    @Test
    fun sessionFormEditRouteRoundTrips() {
        val route = SessionForm(sessionId = "session-1")

        val decoded = json.decodeFromString<SessionForm>(json.encodeToString(route))

        assertEquals("session-1", decoded.sessionId)
    }
}
