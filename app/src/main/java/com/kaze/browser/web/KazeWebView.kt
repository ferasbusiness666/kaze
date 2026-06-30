package com.kaze.browser.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kaze.browser.BrowserViewModel

/** A believable desktop UA so "Desktop mode" actually serves desktop layouts. */
internal const val DESKTOP_UA =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36"

/** Build a configured WebView. Only called the first time a tab actually loads a page. */
@SuppressLint("SetJavaScriptEnabled")
fun buildWebView(context: Context, vm: BrowserViewModel, tab: BrowserTab): WebView =
    WebView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        overScrollMode = View.OVER_SCROLL_NEVER
        // Autofill off — a browser shouldn't pollute the system autofill graph.
        importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO

        with(settings) {
            javaScriptEnabled = true          // required for the modern web
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            mediaPlaybackRequiresUserGesture = true // no surprise autoplay audio
            @Suppress("DEPRECATION")
            setGeolocationEnabled(false)      // feature we don't use
            @Suppress("DEPRECATION")
            saveFormData = false              // no form autofill
            allowFileAccess = false           // web pages can't read local files
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            cacheMode = if (tab.isPrivate) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            userAgentString = if (vm.desktopMode) DESKTOP_UA else userAgentString
        }

        CookieManager.getInstance().setAcceptThirdPartyCookies(this, !tab.isPrivate)

        webViewClient = KazeWebViewClient(vm, tab)
        webChromeClient = KazeChromeClient(tab)
        setDownloadListener { url, _, contentDisposition, mimeType, contentLength ->
            vm.requestDownload(context, url, contentDisposition, mimeType, contentLength)
        }
    }

private class KazeWebViewClient(
    private val vm: BrowserViewModel,
    private val tab: BrowserTab,
) : WebViewClient() {

    // Runs on a background thread — keep it cheap (host set lookup only).
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url?.toString() ?: return null
        return if (AdBlocker.shouldBlock(vm.adBlockEnabled, url)) AdBlocker.blockedResponse() else null
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        val scheme = uri.scheme?.lowercase()
        // Keep web navigation in-app; hand tel:/mailto:/intent: etc. to the system.
        if (scheme != null && scheme != "http" && scheme != "https") {
            runCatching {
                view.context.startActivity(
                    Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
            return true
        }
        return false
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        tab.url = url
        tab.progress = 0
    }

    override fun onPageFinished(view: WebView, url: String?) {
        tab.url = url
        tab.title = view.title?.takeIf { it.isNotBlank() } ?: tab.title
        tab.canGoBack = view.canGoBack()
        tab.canGoForward = view.canGoForward()
        tab.progress = 100
        if (!tab.isPrivate && url != null && url.startsWith("http")) {
            vm.saveHistory(url, tab.title)
        }
    }
}

private class KazeChromeClient(private val tab: BrowserTab) : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        tab.progress = newProgress
    }

    override fun onReceivedTitle(view: WebView, title: String?) {
        if (!title.isNullOrBlank()) tab.title = title
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        // Favicon comes straight from WebView — no image-loading library needed.
        tab.favicon = icon
    }
}

/**
 * Hosts the active tab's WebView. Swapping tabs reparents the existing WebView into
 * this container, resumes it, and pauses every other live WebView (onPause) so
 * background tabs stop burning CPU/GPU.
 */
@Composable
fun WebViewHost(vm: BrowserViewModel, tab: BrowserTab, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx -> FrameLayout(ctx) },
        update = { container ->
            val wv = vm.obtainWebView(tab, container.context)
            if (container.getChildAt(0) !== wv) {
                (wv.parent as? ViewGroup)?.removeView(wv)
                container.removeAllViews()
                container.addView(wv)
            }
            vm.resumeActivePauseOthers(tab)
        },
    )
}
