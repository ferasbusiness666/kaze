package com.kaze.browser.data

import java.net.URLEncoder

/**
 * The four supported engines. Brand colour + letter mirror the design mock; the
 * query template is where a typed search is sent.
 */
enum class SearchEngine(
    val displayName: String,
    val letter: String,
    val color: Long,            // ARGB, e.g. 0xFFDE5833
    private val queryTemplate: String,
) {
    DUCKDUCKGO("DuckDuckGo", "D", 0xFFDE5833, "https://duckduckgo.com/?q=%s"),
    BRAVE("Brave", "B", 0xFFFB542B, "https://search.brave.com/search?q=%s"),
    GOOGLE("Google", "G", 0xFF4285F4, "https://www.google.com/search?q=%s"),
    ECOIA("Ecosia", "E", 0xFF00A550, "https://www.ecosia.org/search?q=%s"),
    START_PAGE("Startpage", "S", 0xFF008060, "https://www.startpage.com/do/dsearch?query=%s"),
    QWANT("Qwant", "Q", 0xFF0066CC, "https://www.qwant.com/?q=%s");

    fun searchUrl(query: String): String =
        queryTemplate.replace("%s", URLEncoder.encode(query, "UTF-8"))

    companion object {
        val DEFAULT = DUCKDUCKGO
        fun fromName(name: String?): SearchEngine =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/** Decides whether typed text is a URL to open or a query to search. Pure + tested. */
object UrlResolver {

    private val SCHEMES = listOf("http://", "https://", "about:", "file:", "data:", "javascript:")

    fun resolve(rawInput: String, engine: SearchEngine): String? {
        val input = rawInput.trim()
        if (input.isEmpty()) return null
        if (SCHEMES.any { input.startsWith(it, ignoreCase = true) }) return input
        return if (looksLikeHost(input)) "https://$input" else engine.searchUrl(input)
    }

    /** A bare host like "example.com", "localhost", or "192.168.0.1[/path]" — no spaces, a real TLD. */
    fun looksLikeHost(input: String): Boolean {
        if (input.any { it.isWhitespace() }) return false
        val host = input.substringBefore('/').substringBefore('?').substringBefore('#')
        if (host.equals("localhost", ignoreCase = true)) return true
        val dot = host.lastIndexOf('.')
        if (dot <= 0 || dot == host.length - 1) return false
        val tld = host.substring(dot + 1)
        // TLD must be all letters (e.g. com, org) or the whole host an IPv4 address.
        return tld.all { it.isLetter() } && tld.length >= 2 || host.split('.').all { p -> p.toIntOrNull() != null }
    }
}
