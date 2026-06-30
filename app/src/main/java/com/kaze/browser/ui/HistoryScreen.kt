package com.kaze.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaze.browser.BrowserViewModel
import com.kaze.browser.R
import com.kaze.browser.Screen
import com.kaze.browser.data.db.HistoryEntry
import com.kaze.browser.ui.theme.KazeTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DAY_FORMAT = DateTimeFormatter.ofPattern("MMM d")

@Composable
fun HistoryScreen(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    val entries by vm.history.collectAsStateWithLifecycle(initialValue = emptyList())
    val groups = remember(entries) { groupByDay(entries) }

    SubScaffold("History", onBack = { vm.go(Screen.BROWSER) }) {
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    "No history yet",
                    color = colors.faint,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            return@SubScaffold
        }

        LazyColumn(Modifier.fillMaxSize().padding(bottom = 24.dp)) {
            groups.forEach { (label, group) ->
                item(key = "header:$label") { GroupHeader(label) }
                items(group, key = { it.id }) { entry ->
                    SwipeRow(
                        entry = entry,
                        onOpen = { vm.navigateTo(entry.url) },
                        onDelete = { vm.deleteHistory(entry.id) },
                    )
                }
            }
            item {
                Text(
                    "Swipe a row left to delete",
                    color = colors.faint,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(label: String) {
    Text(
        label,
        color = KazeTheme.colors.muted,
        fontSize = 12.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
    )
}

@Composable
private fun SwipeRow(entry: HistoryEntry, onOpen: () -> Unit, onDelete: () -> Unit) {
    val colors = KazeTheme.colors
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(colors.danger).padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                KazeIcon(R.drawable.ic_trash, "Delete", tint = androidx.compose.ui.graphics.Color.White, size = 20.dp)
            }
        },
    ) {
        // Opaque background so the red delete layer only shows while swiping.
        Row(
            Modifier.fillMaxWidth().background(colors.bg).clickable(onClick = onOpen)
                .padding(horizontal = 20.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FaviconBadge(null, letterFor(entry.title, entry.url), size = 30.dp, corner = 8.dp, fontSize = 13.sp)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.title,
                    color = colors.fg,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.url,
                    color = colors.faint,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun groupByDay(entries: List<HistoryEntry>): List<Pair<String, List<HistoryEntry>>> {
    val map = LinkedHashMap<String, MutableList<HistoryEntry>>()
    for (entry in entries) {
        map.getOrPut(dayLabel(entry.visitedAt)) { mutableListOf() }.add(entry)
    }
    return map.map { it.key to it.value }
}

private fun dayLabel(epochMs: Long): String {
    val date = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when (date) {
        today -> "TODAY"
        today.minusDays(1) -> "YESTERDAY"
        else -> date.format(DAY_FORMAT).uppercase()
    }
}
