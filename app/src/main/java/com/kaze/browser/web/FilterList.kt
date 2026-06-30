package com.kaze.browser.web

import android.net.Uri

/**
 * A deliberately small EasyList-compatible matcher. It understands the rule forms
 * that make up the vast majority of network filters:
 *
 *   ||doubleclick.net^        -> block that domain and all subdomains
 *   ||ads.example.com^$script -> options after `$` are ignored, domain still blocked
 *   @@||good.example.com^     -> exception (never block)
 *   ! comment / [Adblock...]  -> ignored
 *   ##.cssSelector            -> element-hiding, ignored (we only block network requests)
 *
 * Matching is host-suffix based and allocation-free per request, so it's cheap to
 * call from shouldInterceptRequest on the network thread.
 *
 * ponytail: host-level blocklist, not a full EasyList engine (no regex/option
 * semantics). Upgrade path: ship the real EasyList and extend [parseLine] to honour
 * `$` options and generic URL patterns.
 */
class FilterList private constructor(
    private val blocked: Set<String>,
    private val allowed: Set<String>,
) {

    fun isBlocked(url: String): Boolean {
        val host = runCatching { Uri.parse(url).host }.getOrNull() ?: return false
        return isBlockedHost(host.lowercase())
    }

    fun isBlockedHost(host: String): Boolean {
        if (matchesSuffix(host, allowed)) return false
        return matchesSuffix(host, blocked)
    }

    /** True if host equals, or is a subdomain of, any entry in [set]. */
    private fun matchesSuffix(host: String, set: Set<String>): Boolean {
        if (set.isEmpty()) return false
        var h = host
        while (true) {
            if (h in set) return true
            val i = h.indexOf('.')
            if (i < 0) return false
            h = h.substring(i + 1)
        }
    }

    val size: Int get() = blocked.size

    companion object {
        val EMPTY = FilterList(emptySet(), emptySet())

        fun parse(lines: Sequence<String>): FilterList {
            val blocked = HashSet<String>(8192)
            val allowed = HashSet<String>(256)
            for (raw in lines) {
                val line = raw.trim()
                if (line.isEmpty() || line[0] == '!' || line[0] == '[') continue
                if (line.contains("##") || line.contains("#@#") || line.contains("#?#")) continue
                val isException = line.startsWith("@@")
                val body = if (isException) line.substring(2) else line
                val domain = extractDomain(body) ?: continue
                if (isException) allowed.add(domain) else blocked.add(domain)
            }
            return FilterList(blocked, allowed)
        }

        /** Pull the host out of `||host^...`, `||host/...`, or a bare `host` line. */
        private fun extractDomain(body: String): String? {
            var s = body
            if (s.startsWith("||")) s = s.substring(2)
            // Cut at the first separator that ends a host.
            val end = s.indexOfFirst { it == '^' || it == '/' || it == '*' || it == '$' || it == '?' }
            if (end >= 0) s = s.substring(0, end)
            s = s.trim().lowercase()
            // Must look like a real domain: has a dot, only host-legal characters.
            if (!s.contains('.')) return null
            if (s.any { !(it.isLetterOrDigit() || it == '.' || it == '-' || it == '_') }) return null
            return s
        }
    }
}
