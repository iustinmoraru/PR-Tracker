package com.iustinmoraru.prtracker.ui.common

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iustinmoraru.prtracker.appContainer
import com.iustinmoraru.prtracker.di.AppContainer
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.theme.PrTrackerTheme
import com.iustinmoraru.prtracker.ui.theme.isDark

/**
 * Base class for every activity in the app.
 *
 * It applies the theme chosen by the user (System / Light / Dark), keeps the status and
 * navigation bar icons readable for that theme and draws edge-to-edge.
 */
abstract class ThemedActivity : ComponentActivity() {

    protected val container: AppContainer
        get() = appContainer

    protected fun setThemedContent(content: @Composable () -> Unit) {
        setContent {
            val themeMode by container.settingsRepository.themeMode.collectAsStateWithLifecycle()
            val darkTheme = themeMode.isDark()

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
                )
                onDispose {}
            }

            PrTrackerTheme(darkTheme = darkTheme) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(PrTheme.colors.background)
                ) {
                    content()
                }
            }
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}
