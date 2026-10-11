package com.topjohnwu.magisk.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.google.android.material.color.utilities.Hct
import java.io.File

/**
 * Wallpaper storage, decoding and accent extraction.
 *
 * Deliberately dependency free: the app does not ship an image loading library,
 * so we use the framework decoder and scale down on load to keep memory sane.
 */
object Wallpaper {

    private const val DIR = "theme"
    private const val NAME = "wallpaper"

    fun file(context: Context): File = File(File(context.filesDir, DIR), NAME)

    /** Copy the picked image into private storage, replacing any previous one. */
    fun import(context: Context, uri: Uri): String? = runCatching {
        val dir = File(context.filesDir, DIR)
        dir.mkdirs()
        val name = displayName(context, uri)?.let(::sanitize) ?: NAME
        val target = File(dir, name)
        val tmp = File(dir, "$NAME.tmp")
        context.contentResolver.openInputStream(uri)?.use { input ->
            tmp.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        if (tmp.length() == 0L) {
            tmp.delete()
            return null
        }
        if (target.exists() && !target.delete()) {
            tmp.delete()
            return null
        }
        if (!tmp.renameTo(target)) {
            tmp.delete()
            return null
        }
        // Drop leftovers from earlier picks so the folder never grows unbounded.
        dir.listFiles()?.forEach { if (it != target) it.delete() }
        target.absolutePath
    }.getOrNull()

    private fun displayName(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }.getOrNull()

    private fun sanitize(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(48).ifEmpty { NAME }

    fun delete(context: Context) {
        file(context).delete()
    }

    /** Decode the wallpaper roughly at screen width to bound memory usage. */
    fun load(path: String, reqWidth: Int): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        val target = reqWidth.coerceAtLeast(320)
        while (bounds.outWidth / (sample * 2) >= target &&
            bounds.outHeight / (sample * 2) >= target
        ) {
            sample *= 2
        }
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }.getOrNull()

    /**
     * Pick a representative accent: the hue bucket contributing the most chroma
     * wins, then its pixels are averaged. Falls back to the overall average when
     * the image is basically grey.
     */
    fun dominantSeed(bitmap: Bitmap): Int = runCatching {
        val small = Bitmap.createScaledBitmap(bitmap, 32, 32, true)
        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var total = 0
        val buckets = HashMap<Int, Bucket>()
        for (y in 0 until small.height) {
            for (x in 0 until small.width) {
                val argb = small.getPixel(x, y)
                val r = (argb shr 16) and 0xFF
                val g = (argb shr 8) and 0xFF
                val b = argb and 0xFF
                sumR += r
                sumG += g
                sumB += b
                total++
                val hct = Hct.fromInt(argb)
                if (hct.chroma < 12) continue
                val bucket = buckets.getOrPut((hct.hue / 15).toInt()) { Bucket() }
                bucket.count++
                bucket.r += r
                bucket.g += g
                bucket.b += b
                bucket.chroma += hct.chroma
            }
        }
        if (small !== bitmap) small.recycle()
        val best = buckets.values.maxByOrNull { it.chroma }
        if (best == null || best.count == 0) {
            if (total == 0) return 0
            return argb((sumR / total).toInt(), (sumG / total).toInt(), (sumB / total).toInt())
        }
        argb(
            (best.r / best.count).toInt(),
            (best.g / best.count).toInt(),
            (best.b / best.count).toInt(),
        )
    }.getOrDefault(0)

    private class Bucket {
        var count = 0
        var r = 0L
        var g = 0L
        var b = 0L
        var chroma = 0.0
    }

    private fun argb(r: Int, g: Int, b: Int) =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
