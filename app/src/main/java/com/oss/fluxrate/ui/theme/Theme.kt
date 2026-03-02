package com.oss.fluxrate.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeonGreen,
    secondary = SecondaryNeonPurple,
    tertiary = PrimaryNeonGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onSecondary = TextMain,
    onTertiary = DarkBackground,
    onBackground = TextMain,
    onSurface = TextMain,
    surfaceVariant = SurfaceBorder,
    onSurfaceVariant = TextMuted
)

private val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = PrimaryGreenLight,
    secondary = SecondaryPurpleLight,
    tertiary = PrimaryGreenLight,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = TextMainLight,
    onTertiary = LightSurface,
    onBackground = TextMainLight,
    onSurface = TextMainLight,
    surfaceVariant = SurfaceBorderLight,
    onSurfaceVariant = TextMutedLight
)

@Composable
fun FluxRateTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}