package com.jocmp.capy.accounts.reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FreshRSSIconURLTest {
    private val apiURL = "http://freshrss.example.com:8080/api/greader.php"

    @Test
    fun `rebases f php icons onto the account address`() {
        assertEquals(
            "http://freshrss.example.com:8080/f.php?h=0062855e",
            rebaseFreshRSSIconURL("http://freshrss.example.com/f.php?h=0062855e", apiURL),
        )
    }

    @Test
    fun `keeps a subdirectory install`() {
        assertEquals(
            "https://example.com/freshrss/f.php?h=abc",
            rebaseFreshRSSIconURL(
                "http://localhost/freshrss/f.php?h=abc",
                "https://example.com/freshrss/api/greader.php",
            ),
        )
    }

    @Test
    fun `leaves other icon urls alone`() {
        assertEquals(
            "https://example.com/favicon.ico",
            rebaseFreshRSSIconURL("https://example.com/favicon.ico", apiURL),
        )
    }

    @Test
    fun `leaves icons alone when the api url is not a FreshRSS greader path`() {
        assertEquals(
            "http://host/f.php?h=1",
            rebaseFreshRSSIconURL("http://host/f.php?h=1", "https://reader.example.com/"),
        )
    }

    @Test
    fun `passes through null`() {
        assertNull(rebaseFreshRSSIconURL(null, apiURL))
    }
}
