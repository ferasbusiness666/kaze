package com.kaze.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.BrowserViewModel
import com.kaze.browser.R
import com.kaze.browser.Screen
import com.kaze.browser.ui.theme.KazeTheme
import com.kaze.browser.web.BrowserTab

@Composable
fun TabsScreen(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    SubScaffold("Tabs", onBack = { vm.go(Screen.BROWSER) }) {
        Box(Modifier.fillMaxSize()) {
            if (vm.tabs.isEmpty()) {
                Text(
                    "No open tabs",
                    color = colors.faint,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(vm.tabs, key = { it.id }) { tab ->
                        TabCard(tab, onOpen = { vm.selectTab(tab.id) }, onClose = { vm.closeTab(tab) })
                    }
                }
            }

            ActionRow(
                onCloseAll = { vm.closeAll() },
                onNewTab = { vm.newTab() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun TabCard(tab: BrowserTab, onOpen: () -> Unit, onClose: () -> Unit) {
    val colors = KazeTheme.colors
    Column(
        Modifier.clip(RoundedCornerShape(16.dp)).background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 9.dp, end = 9.dp, top = 9.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FaviconBadge(tab.favicon, letterFor(tab.title, tab.url))
            Spacer(Modifier.width(7.dp))
            Text(
                tab.title,
                color = colors.fg,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.size(18.dp).clip(CircleShape).background(colors.surface2)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                KazeIcon(R.drawable.ic_close, "Close tab", tint = colors.muted, size = 9.dp)
            }
        }
        // Lightweight skeleton preview — we don't keep full-page screenshots in memory.
        Column(
            Modifier.fillMaxWidth().height(118.dp).background(colors.bg).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            SkeletonBar(0.7f, 8.dp)
            SkeletonBar(1f, 6.dp)
            SkeletonBar(0.9f, 6.dp)
            SkeletonBar(0.6f, 6.dp)
        }
    }
}

@Composable
private fun SkeletonBar(fraction: Float, height: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.fillMaxWidth(fraction).height(height)
            .clip(RoundedCornerShape(4.dp)).background(KazeTheme.colors.surface2),
    )
}

@Composable
private fun ActionRow(onCloseAll: () -> Unit, onNewTab: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KazeTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PillButton("Close all", colors.danger, Color.White, onClick = onCloseAll)
        PillButton("New tab", colors.accent, colors.accentFg, icon = R.drawable.ic_plus, onClick = onNewTab)
    }
}

@Composable
private fun PillButton(
    label: String,
    background: Color,
    foreground: Color,
    icon: Int? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier.height(46.dp).clip(RoundedCornerShape(23.dp)).background(background)
            .clickable(onClick = onClick).padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (icon != null) KazeIcon(icon, null, tint = foreground, size = 16.dp)
        Text(label, color = foreground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
