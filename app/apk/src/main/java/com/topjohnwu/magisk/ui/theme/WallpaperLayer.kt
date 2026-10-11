package com.topjohnwu.magisk.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WallpaperState(
    val bitmap: Bitmap? = null,
    val seed: Int = 0,
)

/** Loads the wallpaper off the main thread and derives its accent when needed. */
@Composable
fun rememberWallpaper(config: ThemeConfig): WallpaperState {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val reqWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    var state by remember { mutableStateOf(WallpaperState()) }

    LaunchedEffect(config.wallpaperPath, config.hasWallpaper, config.useWallpaperSeed, reqWidth) {
        if (!config.hasWallpaper) {
            state = WallpaperState()
            return@LaunchedEffect
        }
        state = withContext(Dispatchers.IO) {
            val bitmap = Wallpaper.load(config.wallpaperPath, reqWidth)
            WallpaperState(
                bitmap = bitmap,
                seed = if (bitmap != null && config.useWallpaperSeed) {
                    Wallpaper.dominantSeed(bitmap)
                } else 0,
            )
        }
    }
    return state
}

/** Always composed backdrop: draws nothing unless a wallpaper is active. */
@Composable
fun WallpaperBackdrop(
    bitmap: Bitmap?,
    config: ThemeConfig,
    isDark: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!enabled || bitmap == null) return
    WallpaperLayer(bitmap = bitmap, config = config, isDark = isDark, modifier = modifier)
}

/** Full screen wallpaper with an optional blur and a legibility scrim. */
@Composable
fun WallpaperLayer(
    bitmap: Bitmap,
    config: ThemeConfig,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && config.wallpaperBlur > 0f) {
        Modifier.blur(config.wallpaperBlur.dp)
    } else {
        Modifier
    }
    val scrim = config.wallpaperScrim.coerceIn(0f, 0.9f)

    Box(modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(imageModifier),
        )
        if (scrim > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (isDark) Color.Black.copy(alpha = scrim)
                        else Color.White.copy(alpha = scrim)
                    )
            )
        }
    }
}
