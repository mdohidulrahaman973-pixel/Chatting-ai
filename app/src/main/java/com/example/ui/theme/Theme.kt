package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SipraDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = SpaceBlack,
    primaryContainer = CyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyanBright,
    onSecondary = SpaceBlack,
    secondaryContainer = HudCardSurface,
    onSecondaryContainer = TextSecondary,
    tertiary = AmberTactical,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = DeepMidnight,
    onSurface = TextPrimary,
    surfaceVariant = HudCardSurface,
    onSurfaceVariant = TextSecondary,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun LyraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SipraDarkColorScheme,
        typography = SipraTypography,
        content = content
    )
}

@Composable
fun SipraTheme(
    content: @Composable () -> Unit
) {
    LyraTheme(content = content)
}
