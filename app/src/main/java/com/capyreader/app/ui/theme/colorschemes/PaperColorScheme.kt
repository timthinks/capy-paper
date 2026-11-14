package com.capyreader.app.ui.theme.colorschemes

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Warm charcoal + amber, modeled on the "Paper" theme of the iOS reader Current.
 * Color is used as punctuation: amber marks selection, saved items, links, and toggles.
 */
internal object PaperColorScheme : BaseColorScheme() {
    override val darkScheme = darkColorScheme(
        primary = Color(0xFFD3A573),
        onPrimary = Color(0xFF2A1D10),
        primaryContainer = Color(0xFF4A3826),
        onPrimaryContainer = Color(0xFFF5DEC4),
        inversePrimary = Color(0xFF8A5A2B),
        secondary = Color(0xFFBF9571),
        onSecondary = Color(0xFF2A1D10),
        secondaryContainer = Color(0xFF342F29),
        onSecondaryContainer = Color(0xFFEDE3D6),
        tertiary = Color(0xFF7FB3A8),
        onTertiary = Color(0xFF0E2420),
        tertiaryContainer = Color(0xFF29413C),
        onTertiaryContainer = Color(0xFFCDE8E1),
        background = Color(0xFF1C1914),
        onBackground = Color(0xFFF2ECE4),
        surface = Color(0xFF1C1914),
        onSurface = Color(0xFFF2ECE4),
        surfaceVariant = Color(0xFF2B241C),
        onSurfaceVariant = Color(0xFFA89C8E),
        surfaceTint = Color(0xFFD3A573),
        inverseSurface = Color(0xFFF2ECE4),
        inverseOnSurface = Color(0xFF1C1914),
        outline = Color(0xFF6E655B),
        outlineVariant = Color(0xFF3A342D),
        surfaceContainerLowest = Color(0xFF171410),
        surfaceContainerLow = Color(0xFF231F1A),
        surfaceContainer = Color(0xFF2A2722),
        surfaceContainerHigh = Color(0xFF342F29),
        surfaceContainerHighest = Color(0xFF3D3731),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFB86E2E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF4DEC8),
        onPrimaryContainer = Color(0xFF3B2410),
        inversePrimary = Color(0xFFD3A573),
        secondary = Color(0xFF9A6B45),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEFE6E1),
        onSecondaryContainer = Color(0xFF3B2F2E),
        tertiary = Color(0xFF3F7D71),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD3EBE5),
        onTertiaryContainer = Color(0xFF0E2420),
        background = Color(0xFFFAF6F5),
        onBackground = Color(0xFF3B2F2E),
        surface = Color(0xFFFAF6F5),
        onSurface = Color(0xFF3B2F2E),
        surfaceVariant = Color(0xFFF1E8E2),
        onSurfaceVariant = Color(0xFF7D6E68),
        surfaceTint = Color(0xFFB86E2E),
        inverseSurface = Color(0xFF3B2F2E),
        inverseOnSurface = Color(0xFFFAF6F5),
        outline = Color(0xFFB3A59E),
        outlineVariant = Color(0xFFE6DCD6),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5EFEC),
        surfaceContainer = Color(0xFFF0E9E5),
        surfaceContainerHigh = Color(0xFFEAE2DD),
        surfaceContainerHighest = Color(0xFFE3DAD4),
    )
}
