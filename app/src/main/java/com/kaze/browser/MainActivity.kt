package com.kaze.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaze.browser.ui.BrowserScreen
import com.kaze.browser.ui.DownloadsScreen
import com.kaze.browser.ui.EnginePopover
import com.kaze.browser.ui.HistoryScreen
import com.kaze.browser.ui.OverflowMenu
import com.kaze.browser.ui.SettingsScreen
import com.kaze.browser.ui.TabsScreen
import com.kaze.browser.ui.ToastHost
import com.kaze.browser.ui.theme.KazeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: BrowserViewModel = viewModel()
            KazeRoot(vm)
        }
    }
}

@Composable
private fun KazeRoot(vm: BrowserViewModel) {
    val systemDark = isSystemInDarkTheme()
    KazeTheme(isPrivate = vm.isPrivate, darkTheme = vm.isDarkEffective(systemDark)) {
        BackHandler(enabled = vm.canConsumeBack()) { vm.handleBack() }

        // systemBarsPadding keeps the chrome below the status bar / above the gesture bar.
        Box(Modifier.fillMaxSize().background(KazeTheme.colors.chrome).systemBarsPadding()) {
            when (vm.screen) {
                Screen.BROWSER -> BrowserScreen(vm)
                Screen.TABS -> TabsScreen(vm)
                Screen.SETTINGS -> SettingsScreen(vm)
                Screen.HISTORY -> HistoryScreen(vm)
                Screen.DOWNLOADS -> DownloadsScreen(vm)
            }
            if (vm.menuOpen) OverflowMenu(vm)
            if (vm.enginePopoverOpen) EnginePopover(vm)
            ToastHost(vm)
        }
    }
}
