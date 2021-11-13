/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ThemeColorGeneratorDark : ThemeColorGenerator {
    @ColorInt
    private fun generateColor(seed: Int, s: Int, v: Int): Int {
        return Color.HSVToColor(floatArrayOf((59 * seed % 360).toFloat(), s / 255f, v / 255f))
    }

    private fun extractSeed(title: String): Int {
        return (if (title.isEmpty()) ' ' else title[0]).code
    }

    @ColorInt
    override fun getIconColor(title: String): Int {
        return generateColor(extractSeed(title), 141, 83)
    }

    @ColorInt
    override fun getCollapsedToolbarColor(title: String): Int {
        return generateColor(extractSeed(title), 185, 32)
    }

    @ColorInt
    override fun getExpandedToolbarColor(title: String): Int {
        return generateColor(extractSeed(title), 224, 48)
    }

    @ColorInt
    override fun getSubToolbarColor(title: String): Int {
        return generateColor(extractSeed(title), 185, 48)
    }

    @ColorInt
    override fun getControlColor(title: String): Int {
        return generateColor(extractSeed(title), 192, 32)
    }
}
