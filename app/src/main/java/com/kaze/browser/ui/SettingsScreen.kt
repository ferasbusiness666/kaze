package com.kaze.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.BrowserViewModel
import com.kaze.browser.R
import com.kaze.browser.Screen
import com.kaze.browser.data.SearchEngine
import com.kaze.browser.ui.theme.KazeColors
import com.kaze.browser.ui.theme.KazeTheme

@Composable
fun SettingsScreen(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    val systemDark = isSystemInDarkTheme()

    SubScaffold("Settings", onBack = { vm.go(Screen.BROWSER) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {

            SectionHeader("Search engine")
            SearchEngine.entries.forEach { engine ->
                EngineRow(
                    engine = engine,
                    selected = engine == vm.engine,
                    colors = colors,
                    onSelect = {
                        vm.setEngine(engine)
                        vm.showToast("${engine.displayName} set as default")
                    },
                )
            }

            Divider(colors)
            SectionHeader("Preferences")
            ToggleRow(
                title = "Ad blocker",
                subtitle = "Block ads & trackers",
                checked = vm.adBlockEnabled,
                onCheckedChange = { vm.toggleAdBlock() },
                colors = colors,
            )
            ToggleRow(
                title = "Dark mode",
                subtitle = "Use dark theme",
                checked = vm.isDarkEffective(systemDark),
                onCheckedChange = { vm.setDark(it) },
                colors = colors,
            )

            Divider(colors)
            Row(
                Modifier.fillMaxWidth().clickable { vm.clearHistory() }.padding(horizontal = 20.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazeIcon(R.drawable.ic_trash, null, tint = colors.danger, size = 20.dp)
                Spacer(Modifier.width(14.dp))
                Text("Clear browsing history", color = colors.danger, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        color = KazeTheme.colors.accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
    )
}

@Composable
private fun Divider(colors: KazeColors) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).height(1.dp).background(colors.border))
}

@Composable
private fun EngineRow(engine: SearchEngine, selected: Boolean, colors: KazeColors, onSelect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LetterBadge(engine.letter, Color(engine.color), size = 34.dp, corner = 9.dp, fontSize = 14.sp)
        Spacer(Modifier.width(14.dp))
        Text(engine.displayName, color = colors.fg, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Box(
            Modifier.size(21.dp).clip(CircleShape)
                .border(2.dp, if (selected) colors.accent else colors.faint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(11.dp).clip(CircleShape).background(colors.accent))
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: KazeColors,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.fg, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = colors.muted, fontSize = 12.5.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accent,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.track,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}
