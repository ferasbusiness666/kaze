package com.kaze.browser

import com.kaze.browser.data.SearchEngine
import com.kaze.browser.data.UrlResolver
import com.kaze.browser.web.FilterList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure-logic checks for the two trickiest paths: filter matching and URL/search resolution. */
class LogicTest {

    private val filter = FilterList.parse(
        sequenceOf(
            "! a comment",
            "[Adblock Plus 2.0]",
            "||doubleclick.net^",
            "||ads.example.com^\$script", // options after $ are ignored, domain still blocked
            "@@||good.example.com^",       // exception
            "example.com##.banner",        // element-hiding rule -> ignored
        ),
    )

    @Test fun blocks_domain_and_subdomains() {
        assertTrue(filter.isBlockedHost("doubleclick.net"))
        assertTrue(filter.isBlockedHost("ad.g.doubleclick.net"))
        assertTrue(filter.isBlockedHost("ads.example.com"))
    }

    @Test fun does_not_block_unrelated_or_parent_hosts() {
        assertFalse(filter.isBlockedHost("example.com"))   // only ads.example.com is blocked
        assertFalse(filter.isBlockedHost("net"))           // suffix walk must not match the TLD
        assertFalse(filter.isBlockedHost("notdoubleclick.net"))
    }

    @Test fun exception_wins_over_block() {
        // good.example.com is allow-listed even though ads.example.com is blocked.
        assertFalse(filter.isBlockedHost("good.example.com"))
    }

    @Test fun element_hiding_rules_are_not_treated_as_domains() {
        assertFalse(filter.isBlockedHost("example.com"))
    }

    @Test fun urls_pass_through_unchanged() {
        assertEquals("https://example.com", UrlResolver.resolve("https://example.com", SearchEngine.DUCKDUCKGO))
    }

    @Test fun bare_host_gets_https() {
        assertEquals("https://example.com", UrlResolver.resolve("example.com", SearchEngine.DUCKDUCKGO))
        assertEquals("https://localhost", UrlResolver.resolve("localhost", SearchEngine.GOOGLE))
    }

    @Test fun plain_text_becomes_a_search() {
        val result = UrlResolver.resolve("fast browsers", SearchEngine.DUCKDUCKGO)
        assertTrue(result!!.startsWith("https://duckduckgo.com/?q="))
        assertTrue(result.contains("fast+browsers"))
    }

    @Test fun multiword_with_a_dot_is_still_a_search() {
        val result = UrlResolver.resolve("what is rust.lang", SearchEngine.BING)
        assertTrue(result!!.startsWith("https://www.bing.com/search?q="))
    }

    @Test fun blank_resolves_to_null() {
        assertNull(UrlResolver.resolve("   ", SearchEngine.DUCKDUCKGO))
    }
}
