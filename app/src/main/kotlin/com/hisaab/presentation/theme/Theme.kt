package com.hisaab.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = HB_Black,
    surface = HB_Surface,
    surfaceVariant = HB_Card,
    surfaceContainer = HB_Card,
    surfaceContainerHigh = HB_Elevated,
    surfaceContainerLow = HB_Navy,
    primary = HB_Purple,
    secondary = HB_Cyan,
    tertiary = HB_Amber,
    error = HB_Red,
    onBackground = HB_T1,
    onSurface = HB_T1,
    onSurfaceVariant = HB_T2,
    outline = HB_Border,
    outlineVariant = HB_BorderSubtle
)

@Composable
fun HisaabTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
