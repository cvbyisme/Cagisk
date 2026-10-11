package com.topjohnwu.magisk.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.topjohnwu.magisk.core.R as CoreR

/**
 * Make the whole M3 surface family translucent so the wallpaper shows through.
 *
 * Applying alpha only to `background`/`surface` is not enough: dialogs, bottom
 * sheets, cards and navigation bars are painted with the `surfaceContainer*`
 * tokens, and they would stay opaque next to translucent screens.
 *
 * Text colors (`onSurface` and friends) are intentionally left opaque.
 */
fun ColorScheme.withWallpaperAlpha(config: ThemeConfig): ColorScheme {
    val backgroundAlpha = config.backgroundAlpha.coerceIn(0.05f, 1f)
    val surfaceAlpha = config.surfaceAlpha.coerceIn(0.2f, 1f)
    return copy(
        background = background.copy(alpha = backgroundAlpha),
        surface = surface.copy(alpha = surfaceAlpha),
        surfaceVariant = surfaceVariant.copy(alpha = surfaceAlpha),
        surfaceBright = surfaceBright.copy(alpha = surfaceAlpha),
        surfaceDim = surfaceDim.copy(alpha = surfaceAlpha),
        surfaceContainer = surfaceContainer.copy(alpha = surfaceAlpha),
        surfaceContainerHigh = surfaceContainerHigh.copy(alpha = surfaceAlpha),
        surfaceContainerHighest = surfaceContainerHighest.copy(alpha = surfaceAlpha),
        surfaceContainerLow = surfaceContainerLow.copy(alpha = surfaceAlpha),
        surfaceContainerLowest = surfaceContainerLowest.copy(alpha = surfaceAlpha),
    )
}

/** Accent seed: an explicit user color wins, then the wallpaper derived one. */
fun resolveSeed(config: ThemeConfig, wallpaperSeed: Int): Color? = when {
    config.seedColor != 0 -> Color(config.seedColor)
    config.useWallpaperSeed && wallpaperSeed != 0 -> Color(wallpaperSeed)
    else -> null
}

data class AlphaPreset(
    val label: Int,
    val background: Float,
    val surface: Float,
    val scrim: Float,
)

val AlphaPresets: List<AlphaPreset> = listOf(
    AlphaPreset(CoreR.string.theme_alpha_opaque, 1f, 1f, 0f),
    AlphaPreset(CoreR.string.theme_alpha_balanced, 0.45f, 0.85f, 0.35f),
    AlphaPreset(CoreR.string.theme_alpha_translucent, 0.2f, 0.6f, 0.2f),
)
