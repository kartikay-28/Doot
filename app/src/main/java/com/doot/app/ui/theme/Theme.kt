package com.doot.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DootDarkColorScheme = darkColorScheme(
    primary       = AccentAmber,
    secondary     = AccentTeal,
    error         = AccentRed,
    background    = BgBase,
    surface       = BgPanel,
    onPrimary     = BgBase,
    onSecondary   = BgBase,
    onBackground  = TextPrimary,
    onSurface     = TextPrimary,
    onError       = TextPrimary,
    surfaceVariant = BgPanelAlt,
    outline       = BorderHairline,
)

@Composable
fun DootTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DootDarkColorScheme,
        content = content
    )
}
