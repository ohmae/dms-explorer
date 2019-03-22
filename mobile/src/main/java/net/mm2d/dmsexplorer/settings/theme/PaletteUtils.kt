/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import androidx.palette.graphics.Palette.Swatch

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object PaletteUtils {
    fun selectLightSwatch(palette: androidx.palette.graphics.Palette): Swatch? {
        return palette.vibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.dominantSwatch
    }

    fun selectDarkSwatch(palette: androidx.palette.graphics.Palette): Swatch? {
        return palette.darkVibrantSwatch
            ?: palette.darkMutedSwatch
            ?: palette.vibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.dominantSwatch
    }
}
