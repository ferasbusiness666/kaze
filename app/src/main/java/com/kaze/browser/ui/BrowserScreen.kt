package com.kaze.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.BrowserViewModel
import com.kaze.browser.R
import com.kaze.browser.Screen
import com.kaze.browser.ui.theme.KazeTheme
import com.kaze.browser.web.WebViewHost

@Composable
fun BrowserScreen(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    val active = vm.activeTab

    Column(Modifier.fillMaxSize().background(colors.bg)) {
        if (vm.isPrivate) PrivateBanner()

        TopToolbar(vm)

        // Thin load progress line under the toolbar.
        val progress = active?.progress ?: 0
        if (progress in 1..99) {
            Box(
                Modifier.fillMaxWidth(progress / 100f).height(2.dp).background(colors.accent),
            )
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (active != null && active.url != null) {
                WebViewHost(vm, active, Modifier.fillMaxSize())
            } else {
                StartPage(onOpen = vm::navigateTo)
            }
        }

        BottomBar(vm)
    }
}

@Composable
private fun PrivateBanner() {
    val colors = KazeTheme.colors
    Row(
        Modifier.fillMaxWidth().background(colors.surface).padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KazeIcon(R.drawable.ic_private, null, tint = colors.accent, size = 14.dp)
        Spacer(Modifier.width(7.dp))
        Text("Private browsing", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TopToolbar(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    Row(
        Modifier.fillMaxWidth().background(colors.chrome).padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LetterBadge(
            letter = vm.engine.letter,
            color = Color(vm.engine.color),
            size = 38.dp,
            modifier = Modifier.clickable { vm.enginePopoverOpen = !vm.enginePopoverOpen },
        )
        AddressBar(vm, Modifier.weight(1f))
        TabCountButton(vm.tabCount) { vm.go(Screen.TABS) }
        OverflowButton { vm.menuOpen = true }
    }
}

@Composable
private fun AddressBar(vm: BrowserViewModel, modifier: Modifier = Modifier) {
    val colors = KazeTheme.colors
    val active = vm.activeTab
    val focus = LocalFocusManager.current
    // Reset the field whenever the active tab or its URL changes (e.g. after a load).
    var text by remember(active?.id, active?.url) { mutableStateOf(active?.url ?: "") }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = modifier.height(38.dp),
        singleLine = true,
        textStyle = TextStyle(color = colors.fg, fontSize = 14.sp),
        cursorBrush = SolidColor(colors.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = {
            if (text.isNotBlank()) vm.navigateTo(text)
            focus.clearFocus()
        }),
        decorationBox = { inner ->
            Row(
                Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp)).background(colors.surface)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazeIcon(R.drawable.ic_search, null, tint = colors.faint, size = 14.dp)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (text.isEmpty()) {
                        Text(
                            "Search or type URL",
                            color = colors.faint,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    inner()
                }
            }
        },
    )
}

@Composable
private fun TabCountButton(count: Int, onClick: () -> Unit) {
    val colors = KazeTheme.colors
    Box(
        Modifier.size(38.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(24.dp).border(2.dp, colors.fg, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$count", color = colors.fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OverflowButton(onClick: () -> Unit) {
    val colors = KazeTheme.colors
    Column(
        Modifier.size(width = 34.dp, height = 38.dp).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(3.5.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(3) {
            Box(Modifier.size(4.dp).clip(CircleShape).background(colors.fg))
        }
    }
}

@Composable
private fun BottomBar(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    val active = vm.activeTab
    Row(
        Modifier.fillMaxWidth().background(colors.chrome).padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavButton(R.drawable.ic_back, "Back", enabled = active?.canGoBack == true) { vm.back() }
        NavButton(R.drawable.ic_forward, "Forward", enabled = active?.canGoForward == true) { vm.forward() }
        NavButton(R.drawable.ic_home, "Home") { vm.goStartPage() }
        NavButton(R.drawable.ic_refresh, "Refresh", enabled = active?.url != null) { vm.reload() }
    }
}

@Composable
private fun NavButton(iconRes: Int, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val colors = KazeTheme.colors
    Box(
        Modifier.size(width = 48.dp, height = 44.dp).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        KazeIcon(iconRes, label, tint = if (enabled) colors.fg else colors.faint, size = 23.dp)
    }
}
