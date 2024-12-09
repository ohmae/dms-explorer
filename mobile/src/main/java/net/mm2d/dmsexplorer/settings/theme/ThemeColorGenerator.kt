/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import androidx.annotation.ColorInt

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface ThemeColorGenerator {
    @ColorInt
    fun getIconColor(
        title: String,
    ): Int

    @ColorInt
    fun getCollapsedToolbarColor(
        title: String,
    ): Int

    @ColorInt
    fun getExpandedToolbarColor(
        title: String,
    ): Int

    @ColorInt
    fun getSubToolbarColor(
        title: String,
    ): Int

    @ColorInt
    fun getControlColor(
        title: String,
    ): Int
}
