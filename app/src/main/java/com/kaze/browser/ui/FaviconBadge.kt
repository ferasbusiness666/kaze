package com.kaze.browser.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.ui.theme.KazeTheme

/**
 * Shows a page's real favicon (straight from WebView, no image library) when we
 * have one, otherwise a neutral letter chip. History rows have no bitmap, so they
 * always fall back to the letter.
 */
@Composable
fun FaviconBadge(
    bitmap: Bitmap?,
    fallback: String,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    corner: Dp = 5.dp,
    fontSize: TextUnit = 10.sp,
) {
    val colors = KazeTheme.colors
    val shape = RoundedCornerShape(corner)
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.size(size).clip(shape),
        )
    } else {
        Box(
            modifier = modifier.size(size).clip(shape).background(colors.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                fallback.take(1).uppercase(),
                color = colors.muted,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** First letter for a fallback chip, derived from a title or URL host. */
fun letterFor(title: String?, url: String?): String {
    title?.firstOrNull { it.isLetterOrDigit() }?.let { return it.toString() }
    val host = url?.substringAfter("://")?.substringBefore('/')?.removePrefix("www.")
    return host?.firstOrNull()?.toString() ?: "?"
}
