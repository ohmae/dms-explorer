/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object TextParseUtils {
    @JvmStatic
    fun parseIntSafely(string: String?, defaultValue: Int): Int {
        return string?.toIntOrNull() ?: defaultValue
    }
}