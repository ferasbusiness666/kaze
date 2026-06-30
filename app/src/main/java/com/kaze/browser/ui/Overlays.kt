package com.kaze.browser.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.BrowserViewModel
import com.kaze.browser.R
import com.kaze.browser.Screen
import com.kaze.browser.data.SearchEngine
import com.kaze.browser.ui.theme.KazeTheme
import kotlinx.coroutines.delay

/** Full-screen invisible scrim that dismisses an overlay when tapped. */
@Composable
private fun Scrim(onDismiss: () -> Unit) {
    Box(
        Modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onDismiss,
        ),
    )
}

private fun popoverTop(isPrivate: Boolean) = if (isPrivate) 82.dp else 56.dp

@Composable
fun OverflowMenu(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    val context = LocalContext.current
    Box(Modifier.fillMaxSize()) {
        Scrim { vm.menuOpen = false }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.chrome,
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = 10.dp,
            modifier = Modifier.align(Alignment.TopEnd)
                .padding(top = popoverTop(vm.isPrivate), end = 12.dp).width(254.dp),
        ) {
            Column {
                MenuToggle(R.drawable.ic_shield, "Ad blocker", vm.adBlockEnabled) { vm.toggleAdBlock() }
                MenuToggle(R.drawable.ic_desktop, "Desktop mode", vm.desktopMode) { vm.toggleDesktop() }
                MenuItem(R.drawable.ic_share, "Share page") {
                    val url = vm.activeTab?.url
                    vm.menuOpen = false
                    if (url == null) {
                        vm.showToast("Nothing to share")
                    } else {
                        val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, url)
                        context.startActivity(Intent.createChooser(send, "Share via"))
                    }
                }
                MenuItem(R.drawable.ic_translate, "Translate page") {
                    val url = vm.activeTab?.url
                    vm.menuOpen = false
                    if (url == null) {
                        vm.showToast("Open a page first")
                    } else {
                        vm.navigateTo("https://translate.google.com/translate?sl=auto&tl=en&u=" + Uri.encode(url))
                    }
                }

                MenuDivider()

                MenuItem(
                    icon = R.drawable.ic_private,
                    label = if (vm.isPrivate) "Close private tabs" else "New private tab",
                    tint = colors.accent,
                    textColor = colors.accent,
                ) {
                    if (vm.isPrivate) vm.exitPrivate() else vm.enterPrivate()
                }
                MenuItem(R.drawable.ic_history, "History") { vm.go(Screen.HISTORY) }
                MenuItem(R.drawable.ic_download, "Downloads") { vm.go(Screen.DOWNLOADS) }
                MenuItem(R.drawable.ic_settings, "Settings") { vm.go(Screen.SETTINGS) }
            }
        }
    }
}

@Composable
fun EnginePopover(vm: BrowserViewModel) {
    val colors = KazeTheme.colors
    Box(Modifier.fillMaxSize()) {
        Scrim { vm.enginePopoverOpen = false }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.chrome,
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = 8.dp,
            modifier = Modifier.align(Alignment.TopStart)
                .padding(top = popoverTop(vm.isPrivate), start = 12.dp).width(220.dp),
        ) {
            Column {
                Text(
                    "SEARCH WITH",
                    color = colors.muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 6.dp),
                )
                SearchEngine.entries.forEach { engine ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            vm.setEngine(engine)
                            vm.enginePopoverOpen = false
                        }.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LetterBadge(engine.letter, Color(engine.color), size = 26.dp, corner = 7.dp, fontSize = 12.sp)
                        Spacer(Modifier.width(11.dp))
                        Text(engine.displayName, color = colors.fg, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        if (engine == vm.engine) {
                            KazeIcon(R.drawable.ic_check, null, tint = colors.accent, size = 16.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToastHost(vm: BrowserViewModel) {
    val message = vm.toast ?: return
    LaunchedEffect(message) {
        delay(1800)
        vm.dismissToast()
    }
    Box(Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF0D1B2A), shadowElevation = 6.dp) {
            Text(
                message,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: Int,
    label: String,
    tint: Color = KazeTheme.colors.fg,
    textColor: Color = KazeTheme.colors.fg,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KazeIcon(icon, null, tint = tint, size = 19.dp)
        Spacer(Modifier.width(12.dp))
        Text(label, color = textColor, fontSize = 14.5.sp)
    }
}

@Composable
private fun MenuToggle(icon: Int, label: String, checked: Boolean, onToggle: () -> Unit) {
    val colors = KazeTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KazeIcon(icon, null, tint = colors.fg, size = 19.dp)
        Spacer(Modifier.width(12.dp))
        Text(label, color = colors.fg, fontSize = 14.5.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
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

@Composable
private fun MenuDivider() {
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(1.dp).background(KazeTheme.colors.border))
}
