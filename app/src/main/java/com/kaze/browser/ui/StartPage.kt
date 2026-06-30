package com.kaze.browser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.R
import com.kaze.browser.ui.theme.KazeTheme

private data class Shortcut(val letter: String, val label: String, val url: String, val color: Long)

private val SHORTCUTS = listOf(
    Shortcut("D", "DuckDuckGo", "https://duckduckgo.com", 0xFFDE5833),
    Shortcut("W", "Wikipedia", "https://wikipedia.org", 0xFF3366CC),
    Shortcut("G", "GitHub", "https://github.com", 0xFF24292E),
    Shortcut("Y", "YouTube", "https://youtube.com", 0xFFFF0000),
)

@Composable
fun StartPage(onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = KazeTheme.colors
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        KazeIcon(R.drawable.ic_logo, contentDescription = "Kaze", tint = colors.fg, size = 58.dp)
        Spacer(Modifier.height(16.dp))
        Text("kaze", color = colors.fg, fontSize = 30.sp, fontWeight = FontWeight.Light)

        Spacer(Modifier.height(40.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            SHORTCUTS.forEach { s ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LetterBadge(
                        letter = s.letter,
                        color = Color(s.color),
                        size = 50.dp,
                        corner = 16.dp,
                        fontSize = 19.sp,
                        modifier = Modifier.clickable { onOpen(s.url) },
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        s.label,
                        color = colors.muted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(64.dp),
                    )
                }
            }
        }
    }
}
