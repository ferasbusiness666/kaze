package com.kaze.browser.web

import android.content.Context
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

/**
 * Holds the parsed [FilterList] and answers blocking questions from the WebView's
 * network thread. The list is parsed off the main thread on first init.
 */
object AdBlocker {

    @Volatile
    var filter: FilterList = FilterList.EMPTY
        private set

    @Volatile
    private var started = false

    /** Parse assets/adblock.txt once, in the background. Safe to call repeatedly. */
    @Synchronized
    fun init(context: Context) {
        if (started) return
        started = true
        val assets = context.applicationContext.assets
        Thread {
            filter = runCatching {
                assets.open("adblock.txt").bufferedReader().useLines { FilterList.parse(it) }
            }.getOrDefault(FilterList.EMPTY)
        }.apply { isDaemon = true; name = "adblock-load" }.start()
    }

    fun shouldBlock(enabled: Boolean, url: String): Boolean =
        enabled && filter.isBlocked(url)

    /** Empty 200 body returned for a blocked request so the page just sees nothing. */
    fun blockedResponse(): WebResourceResponse =
        WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
}
