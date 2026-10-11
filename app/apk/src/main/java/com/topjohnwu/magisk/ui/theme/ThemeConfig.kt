package com.topjohnwu.magisk.ui.theme

import com.topjohnwu.magisk.core.Config
import java.io.File

/**
 * User tweakable theme: wallpaper, accent seed and UI opacity.
 *
 * Every field is backed by [Config] preferences so it survives restarts; the
 * dialog mutates [ThemeState.config] for instant preview and persists from here.
 */
data class ThemeConfig(
    val wallpaperEnabled: Boolean = false,
    val wallpaperPath: String = "",
    val wallpaperBlur: Float = 8f,
    val wallpaperScrim: Float = 0.35f,
    val backgroundAlpha: Float = 0.45f,
    val surfaceAlpha: Float = 0.85f,
    val seedColor: Int = 0,
    val useWallpaperSeed: Boolean = false,
) {
    /** Only show the wallpaper when it is enabled *and* the file is still there. */
    val hasWallpaper: Boolean
        get() = wallpaperEnabled && wallpaperPath.isNotEmpty() && File(wallpaperPath).isFile

    val wallpaperName: String
        get() = File(wallpaperPath).name

    companion object {
        fun load() = ThemeConfig(
            wallpaperEnabled = Config.wallpaperEnabled,
            wallpaperPath = Config.wallpaperPath,
            wallpaperBlur = Config.wallpaperBlur,
            wallpaperScrim = Config.wallpaperScrim,
            backgroundAlpha = Config.themeBackgroundAlpha,
            surfaceAlpha = Config.themeSurfaceAlpha,
            seedColor = Config.themeSeedColor,
            useWallpaperSeed = Config.themeWallpaperSeed,
        )

        fun save(config: ThemeConfig) {
            Config.wallpaperEnabled = config.wallpaperEnabled
            Config.wallpaperPath = config.wallpaperPath
            Config.wallpaperBlur = config.wallpaperBlur
            Config.wallpaperScrim = config.wallpaperScrim
            Config.themeBackgroundAlpha = config.backgroundAlpha
            Config.themeSurfaceAlpha = config.surfaceAlpha
            Config.themeSeedColor = config.seedColor
            Config.themeWallpaperSeed = config.useWallpaperSeed
        }
    }
}

/** Accent presets; [ThemeConfig.seedColor] of 0 means "keep the Magisk default". */
val ThemeSeedPresets: List<Int> = listOf(
    0,
    0xFF00AF9C.toInt(),
    0xFF7C4DFF.toInt(),
    0xFF3D5AFE.toInt(),
    0xFF00B0FF.toInt(),
    0xFF00C853.toInt(),
    0xFFFF6D00.toInt(),
    0xFFEC407A.toInt(),
)
