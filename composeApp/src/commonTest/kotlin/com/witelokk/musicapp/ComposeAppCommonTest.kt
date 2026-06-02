package com.witelokk.musicapp

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppCommonTest {

    @Test
    fun `server health url does not duplicate health path`() {
        val url = "http://192.168.1.133/api/v1//health/api/v1/health/api/v1/health"

        assertEquals(
            "http://192.168.1.133/api/v1/health",
            serverHealthUrl(url),
        )
    }

    @Test
    fun `server health url handles trailing slash`() {
        assertEquals(
            "http://192.168.1.133/api/v1/health",
            serverHealthUrl("http://192.168.1.133/api/v1/"),
        )
    }

    @Test
    fun `blank server url falls back to default health url`() {
        assertEquals(
            "${DEFAULT_BASE_URL.trimEnd('/')}/health",
            serverHealthUrl(""),
        )
    }

    @Test
    fun `health in host name is preserved`() {
        assertEquals(
            "https://health.example.com/api/v1/health",
            serverHealthUrl("https://health.example.com/api/v1"),
        )
    }
}
