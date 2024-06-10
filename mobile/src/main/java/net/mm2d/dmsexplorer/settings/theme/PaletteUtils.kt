/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object PaletteUtils {
    fun selectLightSwatch(palette: Palette): Swatch? =
        palette.vibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.dominantSwatch

    fun selectDarkSwatch(palette: Palette): Swatch? =
        palette.darkVibrantSwatch
            ?: palette.darkMutedSwatch
            ?: palette.vibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.dominantSwatch
}

fun Bitmap.generatePalette(): Palette = Palette.Builder(this).generate()

fun Bitmap.generatePalette(listener: (Palette?) -> Unit) {
    Palette.Builder(this).generate(listener)
}
