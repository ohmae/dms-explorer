/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * テーマとしての色を決定するメソッドを持つクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ColorUtils {
    private const val DARKER_RATIO = 0.7f

    @ColorInt
    fun getDarkerColor(
        @ColorInt color: Int,
        ratio: Float = DARKER_RATIO,
    ): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * ratio + 0.5f).toInt()
        val g = (Color.green(color) * ratio + 0.5f).toInt()
        val b = (Color.blue(color) * ratio + 0.5f).toInt()
        return Color.argb(a, r, g, b)
    }
}
