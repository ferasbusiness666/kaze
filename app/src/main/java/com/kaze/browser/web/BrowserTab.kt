package com.kaze.browser.web

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * One tab. The [webView] is created lazily — a brand-new tab showing the start page
 * holds no WebView at all, which keeps memory and startup cost down. Observable
 * fields drive the toolbar / tab grid via Compose snapshot state.
 */
class BrowserTab(
    val id: Long,
    val isPrivate: Boolean,
) {
    var webView: WebView? = null

    /** null = show the start page (no page loaded yet). */
    var url by mutableStateOf<String?>(null)
    var title by mutableStateOf("New tab")
    var favicon by mutableStateOf<Bitmap?>(null)
    var progress by mutableIntStateOf(0)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)

    fun destroy() {
        webView?.apply {
            stopLoading()
            (parent as? android.view.ViewGroup)?.removeView(this)
            removeAllViews()
            destroy()
        }
        webView = null
    }
}
