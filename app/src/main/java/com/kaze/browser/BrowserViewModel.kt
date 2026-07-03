package com.kaze.browser

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaze.browser.data.Prefs
import com.kaze.browser.data.SearchEngine
import com.kaze.browser.data.ThemeMode
import com.kaze.browser.data.UrlResolver
import com.kaze.browser.data.db.DownloadEntry
import com.kaze.browser.data.db.HistoryEntry
import com.kaze.browser.web.BrowserTab
import com.kaze.browser.web.DESKTOP_UA
import com.kaze.browser.web.buildWebView
import kotlinx.coroutines.launch

/** Which full-screen view is showing. The toolbar/start-page/web view all live under BROWSER. */
enum class Screen { BROWSER, TABS, SETTINGS, HISTORY, DOWNLOADS }

/**
 * Single source of truth for the whole UI: tabs (+ their WebViews), navigation,
 * persisted settings, private mode, and the history/downloads data streams.
 */
class BrowserViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val db = (app as KazeApp).db

    // ---- persisted settings ----
    var engine by mutableStateOf(prefs.engine)
        private set
    var adBlockEnabled by mutableStateOf(prefs.adBlock)
        private set
    var themeMode by mutableStateOf(prefs.theme)
        private set

    // ---- session-only UI state ----
    var desktopMode by mutableStateOf(false)
        private set
    var isPrivate by mutableStateOf(false)
        private set
    var screen by mutableStateOf(Screen.BROWSER)
        private set
    var menuOpen by mutableStateOf(false)
    var enginePopoverOpen by mutableStateOf(false)
    var toast by mutableStateOf<String?>(null)
        private set

    // ---- tabs (normal + private kept separate so "Close private tabs" is clean) ----
    val normalTabs = mutableStateListOf<BrowserTab>()
    val privateTabs = mutableStateListOf<BrowserTab>()
    private var activeNormalId by mutableLongStateOf(-1L)
    private var activePrivateId by mutableLongStateOf(-1L)
    private var nextId = 1L

    // ---- data streams for the history / downloads screens ----
    val history = db.historyDao().all()
    val downloads = db.downloadDao().all()

    val tabs get() = if (isPrivate) privateTabs else normalTabs
    private val activeId get() = if (isPrivate) activePrivateId else activeNormalId
    val activeTab: BrowserTab? get() = tabs.firstOrNull { it.id == activeId }
    val tabCount get() = tabs.size

    init {
        if (normalTabs.isEmpty()) newTab()
    }

    // ---------- tab management ----------
    fun newTab(): BrowserTab {
        val list = tabs
        if (list.size >= MAX_TABS) {
            showToast("Up to $MAX_TABS tabs")
            return activeTab ?: list.last()
        }
        val tab = BrowserTab(nextId++, isPrivate)
        list.add(tab)
        setActiveId(tab.id)
        screen = Screen.BROWSER
        menuOpen = false
        return tab
    }

    fun closeTab(tab: BrowserTab) {
        val list = tabs
        val idx = list.indexOf(tab)
        if (idx < 0) return
        tab.destroy()
        list.removeAt(idx)
        if (activeId == tab.id) {
            setActiveId(list.getOrNull(idx)?.id ?: list.lastOrNull()?.id ?: -1L)
        }
    }

    fun closeAll() {
        tabs.forEach { it.destroy() }
        tabs.clear()
        setActiveId(-1L)
    }

    fun selectTab(id: Long) {
        setActiveId(id)
        screen = Screen.BROWSER
    }

    private fun setActiveId(id: Long) {
        if (isPrivate) activePrivateId = id else activeNormalId = id
    }

    // ---------- navigation ----------
    fun go(target: Screen) {
        // Leaving the browser view: pause the visible WebView so a sub-screen costs nothing.
        if (target != Screen.BROWSER) activeTab?.webView?.onPause()
        screen = target
        menuOpen = false
        enginePopoverOpen = false
    }

    fun navigateTo(input: String) {
        val tab = activeTab ?: newTab()
        val url = UrlResolver.resolve(input, engine) ?: return
        tab.url = url
        tab.webView?.loadUrl(url) // if null, WebViewHost will create it and load tab.url
        screen = Screen.BROWSER
        menuOpen = false
        enginePopoverOpen = false
    }

    /** True when the system back press has something to consume (so we don't exit the app). */
    fun canConsumeBack(): Boolean =
        menuOpen || enginePopoverOpen || screen != Screen.BROWSER || activeTab?.canGoBack == true

    /** Handle a system back press in priority order: overlays → sub-screens → web history. */
    fun handleBack() {
        when {
            menuOpen -> menuOpen = false
            enginePopoverOpen -> enginePopoverOpen = false
            screen != Screen.BROWSER -> screen = Screen.BROWSER
            else -> back()
        }
    }

    fun back() = activeTab?.webView?.let { if (it.canGoBack()) it.goBack() }
    fun forward() = activeTab?.webView?.let { if (it.canGoForward()) it.goForward() }
    fun reload() {
        activeTab?.webView?.reload()
    }

    /** Bottom "home" button: drop back to the start page but keep the tab/WebView alive. */
    fun goStartPage() {
        activeTab?.let {
            it.url = null
            it.webView?.onPause()
        }
        screen = Screen.BROWSER
        menuOpen = false
    }

    // ---------- WebView lifecycle ----------
    fun obtainWebView(tab: BrowserTab, context: Context): WebView {
        tab.webView?.let { return it }
        val wv = buildWebView(context, this, tab)
        tab.webView = wv
        tab.url?.let { wv.loadUrl(it) }
        return wv
    }

    /** Keep only the visible tab running; pause everything else to save CPU/memory. */
    fun resumeActivePauseOthers(active: BrowserTab) {
        normalTabs.forEach { it.webView?.let { wv -> if (it === active) wv.onResume() else wv.onPause() } }
        privateTabs.forEach { it.webView?.let { wv -> if (it === active) wv.onResume() else wv.onPause() } }
    }

    // ---------- settings ----------
    fun toggleAdBlock() {
        adBlockEnabled = !adBlockEnabled
        prefs.adBlock = adBlockEnabled
    }

    fun updateEngine(e: SearchEngine) {
        engine = e
        prefs.engine = e
    }

    fun setDark(dark: Boolean) {
        themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT
        prefs.theme = themeMode
    }

    fun isDarkEffective(systemDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    fun toggleDesktop() {
        desktopMode = !desktopMode
        val ua = if (desktopMode) DESKTOP_UA else null
        (normalTabs + privateTabs).forEach { t ->
            t.webView?.settings?.apply {
                userAgentString = ua // null restores the default mobile UA
                useWideViewPort = true
                loadWithOverviewMode = true
            }
        }
        activeTab?.webView?.reload()
        showToast(if (desktopMode) "Desktop site" else "Mobile site")
    }

    // ---------- private mode ----------
    fun enterPrivate() {
        if (isPrivate) return
        isPrivate = true
        menuOpen = false
        if (privateTabs.isEmpty()) newTab() else { screen = Screen.BROWSER }
    }

    fun exitPrivate() {
        privateTabs.forEach { it.destroy() }
        privateTabs.clear()
        activePrivateId = -1L
        isPrivate = false
        menuOpen = false
        screen = Screen.BROWSER
        // Drop per-session cookies left by private browsing (persistent cookies are untouched).
        CookieManager.getInstance().removeSessionCookies(null)
        CookieManager.getInstance().flush()
        if (normalTabs.isEmpty()) newTab()
    }

    // ---------- history (skipped entirely in private mode) ----------
    fun saveHistory(url: String, title: String) {
        viewModelScope.launch {
            db.historyDao().insert(HistoryEntry(title = title, url = url, visitedAt = System.currentTimeMillis()))
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch { db.historyDao().delete(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { db.historyDao().clear() }
        showToast("History cleared")
    }

    // ---------- downloads (system DownloadManager + a local record) ----------
    fun requestDownload(
        context: Context,
        url: String,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
    ) {
        runCatching {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                setTitle(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
            }
            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            viewModelScope.launch {
                db.downloadDao().insert(
                    DownloadEntry(
                        fileName = fileName,
                        url = url,
                        mimeType = mimeType,
                        sizeBytes = contentLength,
                        downloadedAt = System.currentTimeMillis(),
                    ),
                )
            }
            showToast("Downloading $fileName")
        }.onFailure { showToast("Download failed") }
    }

    // ---------- toast ----------
    fun showToast(message: String) {
        toast = message
    }

    fun dismissToast() {
        toast = null
    }

    override fun onCleared() {
        (normalTabs + privateTabs).forEach { it.destroy() }
    }

    private companion object {
        const val MAX_TABS = 4
    }
}
