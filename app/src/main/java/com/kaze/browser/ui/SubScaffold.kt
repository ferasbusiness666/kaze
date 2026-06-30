package com.kaze.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.R
import com.kaze.browser.ui.theme.KazeTheme

/** Shared back-arrow + title app bar used by Tabs / Settings / History / Downloads. */
@Composable
fun SubScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    val colors = KazeTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg)) {
        Row(
            Modifier.fillMaxWidth().background(colors.chrome)
                .padding(start = 6.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(44.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                KazeIcon(R.drawable.ic_arrow_back, "Back", tint = colors.fg)
            }
            Text(title, color = colors.fg, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}
