package com.topjohnwu.magisk.ui.theme

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.core.ktx.toast
import com.topjohnwu.magisk.ui.MagiskAccentColor
import com.topjohnwu.magisk.ui.ThemeState
import com.topjohnwu.magisk.ui.component.MagiskDialog
import com.topjohnwu.magisk.ui.component.SettingsArrow
import com.topjohnwu.magisk.ui.component.SettingsSwitch
import kotlin.math.roundToInt
import com.topjohnwu.magisk.core.R as CoreR

/** Wallpaper, accent color and UI opacity in one place. */
@Composable
fun ThemeSettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val config = ThemeState.config
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun apply(updated: ThemeConfig) = ThemeState.update(updated)

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val path = Wallpaper.import(context, uri)
        if (path == null) {
            context.toast(CoreR.string.theme_wallpaper_failed, Toast.LENGTH_SHORT)
        } else {
            apply(config.copy(wallpaperPath = path, wallpaperEnabled = true))
        }
    }

    MagiskDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = stringResource(CoreR.string.theme_customization),
        confirmText = stringResource(android.R.string.ok),
        onConfirm = onDismiss,
        dismissText = stringResource(android.R.string.cancel),
        onDismiss = onDismiss,
        scrollable = true,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            // --- Wallpaper ---
            SettingsSwitch(
                title = stringResource(CoreR.string.theme_wallpaper_enable),
                summary = stringResource(CoreR.string.theme_wallpaper_enable_summary),
                checked = config.wallpaperEnabled,
                onCheckedChange = { apply(config.copy(wallpaperEnabled = it)) },
            )
            SettingsArrow(
                title = stringResource(CoreR.string.theme_wallpaper_pick),
                summary = if (config.wallpaperPath.isEmpty()) {
                    stringResource(CoreR.string.theme_wallpaper_none)
                } else {
                    config.wallpaperName
                },
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            )
            if (config.wallpaperPath.isNotEmpty() && config.wallpaperEnabled) {
                SettingsArrow(
                    title = stringResource(CoreR.string.theme_wallpaper_remove),
                    onClick = {
                        Wallpaper.delete(context)
                        apply(config.copy(wallpaperPath = ""))
                    },
                )
            }

            if (config.hasWallpaper) {
                Divider()
                SliderRow(
                    title = stringResource(CoreR.string.theme_wallpaper_blur),
                    value = config.wallpaperBlur,
                    valueRange = 0f..40f,
                    valueText = if (blurSupported) {
                        "${config.wallpaperBlur.roundToInt()} dp"
                    } else {
                        stringResource(CoreR.string.theme_blur_unsupported)
                    },
                    enabled = blurSupported,
                    onValueChange = { ThemeState.preview(ThemeState.config.copy(wallpaperBlur = it)) },
                    onValueChangeFinished = { ThemeState.commit() },
                )
                SliderRow(
                    title = stringResource(CoreR.string.theme_wallpaper_scrim),
                    value = config.wallpaperScrim,
                    valueRange = 0f..0.8f,
                    valueText = "${(config.wallpaperScrim * 100).roundToInt()}%",
                    onValueChange = { ThemeState.preview(ThemeState.config.copy(wallpaperScrim = it)) },
                    onValueChangeFinished = { ThemeState.commit() },
                )
                Divider()
            }

            // --- Accent color ---
            SectionTitle(stringResource(CoreR.string.theme_accent_color))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThemeSeedPresets.forEach { preset ->
                    val selected = config.seedColor == preset
                    val color = if (preset == 0) MagiskAccentColor else Color(preset)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            )
                            .clickable { apply(config.copy(seedColor = preset)) }
                    )
                }
            }
            Text(
                text = stringResource(CoreR.string.theme_accent_default_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            SettingsSwitch(
                title = stringResource(CoreR.string.theme_accent_from_wallpaper),
                summary = stringResource(CoreR.string.theme_accent_from_wallpaper_summary),
                checked = config.useWallpaperSeed,
                enabled = config.wallpaperPath.isNotEmpty(),
                onCheckedChange = { apply(config.copy(useWallpaperSeed = it)) },
            )
            Divider()

            // --- Opacity ---
            SectionTitle(stringResource(CoreR.string.theme_opacity))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AlphaPresets.forEach { preset ->
                    val selected = config.backgroundAlpha == preset.background &&
                        config.surfaceAlpha == preset.surface
                    FilterChip(
                        selected = selected,
                        onClick = {
                            apply(
                                config.copy(
                                    backgroundAlpha = preset.background,
                                    surfaceAlpha = preset.surface,
                                    wallpaperScrim = preset.scrim,
                                )
                            )
                        },
                        label = { Text(stringResource(preset.label)) },
                    )
                }
            }
            SliderRow(
                title = stringResource(CoreR.string.theme_background_alpha),
                value = config.backgroundAlpha,
                valueRange = 0.05f..1f,
                valueText = "${(config.backgroundAlpha * 100).roundToInt()}%",
                enabled = config.hasWallpaper,
                onValueChange = { ThemeState.preview(ThemeState.config.copy(backgroundAlpha = it)) },
                onValueChangeFinished = { ThemeState.commit() },
            )
            SliderRow(
                title = stringResource(CoreR.string.theme_surface_alpha),
                value = config.surfaceAlpha,
                valueRange = 0.2f..1f,
                valueText = "${(config.surfaceAlpha * 100).roundToInt()}%",
                enabled = config.hasWallpaper,
                onValueChange = { ThemeState.preview(ThemeState.config.copy(surfaceAlpha = it)) },
                onValueChangeFinished = { ThemeState.commit() },
            )
            Text(
                text = stringResource(CoreR.string.theme_opacity_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun SliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}
