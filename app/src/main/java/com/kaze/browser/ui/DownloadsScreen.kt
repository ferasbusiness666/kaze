package com.kaze.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaze.browser.BrowserViewModel
import com.kaze.browser.R
import com.kaze.browser.Screen
import com.kaze.browser.data.db.DownloadEntry
import com.kaze.browser.ui.theme.KazeTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, HH:mm")

@Composable
fun DownloadsScreen(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    val entries by vm.downloads.collectAsStateWithLifecycle(initialValue = emptyList())

    SubScaffold("Downloads", onBack = { vm.go(Screen.BROWSER) }) {
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    "No downloads",
                    color = colors.faint,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            return@SubScaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(vertical = 8.dp)) {
            items(entries, key = { it.id }) { entry -> DownloadRow(entry) }
        }
    }
}

@Composable
private fun DownloadRow(entry: DownloadEntry) {
    val colors = KazeTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(colors.surface2),
            contentAlignment = Alignment.Center,
        ) {
            KazeIcon(fileTypeIcon(entry.fileName, entry.mimeType), null, tint = colors.fg, size = 22.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.fileName,
                color = colors.fg,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(2.dp))
            Text(
                "${formatSize(entry.sizeBytes)} · ${formatDate(entry.downloadedAt)}",
                color = colors.muted,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.width(8.dp))
        KazeIcon(R.drawable.ic_download, null, tint = colors.faint, size = 20.dp)
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var i = 0
    while (value >= 1024 && i < units.lastIndex) {
        value /= 1024; i++
    }
    return if (i == 0) "$bytes B" else String.format(Locale.US, "%.1f %s", value, units[i])
}

private fun formatDate(epochMs: Long): String =
    Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).format(DATE_FORMAT)
