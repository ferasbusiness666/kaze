package com.kaze.browser.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** App colour tokens, lifted straight from the design's THEMES table. */
data class KazeColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val fg: Color,
    val muted: Color,
    val faint: Color,
    val border: Color,
    val accent: Color,
    val accentFg: Color,
    val chrome: Color,
    val statusText: Color,
    val track: Color,
    val isDark: Boolean,
) {
    val danger: Color get() = Color(0xFFD64545) // shared "delete" red
}

private val LightColors = KazeColors(
    bg = Color(0xFFFFFFFF), surface = Color(0xFFF4F5F7), surface2 = Color(0xFFE9EBEF),
    fg = Color(0xFF0D1B2A), muted = Color(0xFF5C6675), faint = Color(0xFF98A1AE),
    border = Color(0xFFE6E8EC), accent = Color(0xFF0D1B2A), accentFg = Color(0xFFFFFFFF),
    chrome = Color(0xFFFFFFFF), statusText = Color(0xFF0D1B2A), track = Color(0xFFD2D6DD),
    isDark = false,
)

private val DarkColors = KazeColors(
    bg = Color(0xFF1E1E1E), surface = Color(0xFF272727), surface2 = Color(0xFF333333),
    fg = Color(0xFFFFFFFF), muted = Color(0xFFA7A7A7), faint = Color(0xFF6E6E6E),
    border = Color(0xFF3A3A3A), accent = Color(0xFF6E90C8), accentFg = Color(0xFF10243C),
    chrome = Color(0xFF1E1E1E), statusText = Color(0xFFFFFFFF), track = Color(0xFF3C3C3C),
    isDark = true,
)

private val PrivateColors = KazeColors(
    bg = Color(0xFF171221), surface = Color(0xFF221A33), surface2 = Color(0xFF2D2342),
    fg = Color(0xFFFFFFFF), muted = Color(0xFFBCB0D6), faint = Color(0xFF7E7298),
    border = Color(0xFF352A4C), accent = Color(0xFF9D7CE8), accentFg = Color(0xFF1A1226),
    chrome = Color(0xFF171221), statusText = Color(0xFFEDE7F7), track = Color(0xFF3A2F52),
    isDark = true,
)

private val LocalKazeColors = staticCompositionLocalOf { LightColors }

object KazeTheme {
    val colors: KazeColors
        @Composable get() = LocalKazeColors.current
}

@Composable
fun KazeTheme(
    isPrivate: Boolean,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = when {
        isPrivate -> PrivateColors
        darkTheme -> DarkColors
        else -> LightColors
    }

    // Tint the system status/nav bars to match the chrome colour.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.chrome.toArgb()
            window.navigationBarColor = colors.chrome.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !colors.isDark
                isAppearanceLightNavigationBars = !colors.isDark
            }
        }
    }

    // Feed Material3 components (Switch, ripple, text selection) a matching scheme.
    val scheme = (if (colors.isDark) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.accent,
        onPrimary = colors.accentFg,
        background = colors.bg,
        onBackground = colors.fg,
        surface = colors.surface,
        onSurface = colors.fg,
        surfaceVariant = colors.surface2,
        outline = colors.border,
        error = colors.danger,
    )

    CompositionLocalProvider(LocalKazeColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
