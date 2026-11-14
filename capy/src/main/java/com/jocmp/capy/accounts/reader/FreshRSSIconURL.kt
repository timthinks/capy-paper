package com.jocmp.capy.accounts.reader

import java.net.URI

/**
 * FreshRSS builds `iconUrl` from its configured `base_url`, which is often wrong
 * behind a port mapping or when reached over a different address (VPN, LAN).
 * Icons are served by `f.php` next to the API, so rebase them onto the address
 * the account actually syncs with.
 */
internal fun rebaseFreshRSSIconURL(iconURL: String?, apiURL: String): String? {
    if (iconURL.isNullOrBlank()) {
        return iconURL
    }

    val icon = runCatching { URI(iconURL) }.getOrNull() ?: return iconURL

    if (!icon.path.orEmpty().endsWith("/f.php")) {
        return iconURL
    }

    val apiMarker = apiURL.indexOf(API_PATH)

    if (apiMarker < 0) {
        return iconURL
    }

    val root = apiURL.substring(0, apiMarker)
    val query = icon.rawQuery?.let { "?$it" }.orEmpty()

    return "$root/f.php$query"
}

private const val API_PATH = "/api/greader.php"
