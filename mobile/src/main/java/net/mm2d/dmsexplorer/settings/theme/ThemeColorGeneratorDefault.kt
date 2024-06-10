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
internal class ThemeColorGeneratorDefault : ThemeColorGenerator {
    @ColorInt
    private fun generateColor(seed: Int, s: Int, v: Int): Int =
        Color.HSVToColor(floatArrayOf((59 * seed % 360).toFloat(), s / 255f, v / 255f))

    private fun extractSeed(title: String): Int =
        (if (title.isEmpty()) ' ' else title[0]).code

    @ColorInt
    override fun getIconColor(title: String): Int =
        generateColor(extractSeed(title), 111, 237)

    @ColorInt
    override fun getCollapsedToolbarColor(title: String): Int =
        generateColor(extractSeed(title), 185, 187)

    @ColorInt
    override fun getExpandedToolbarColor(title: String): Int =
        generateColor(extractSeed(title), 124, 210)

    @ColorInt
    override fun getSubToolbarColor(title: String): Int =
        generateColor(extractSeed(title), 34, 248)

    @ColorInt
    override fun getControlColor(title: String): Int =
        generateColor(extractSeed(title), 192, 96)
}
