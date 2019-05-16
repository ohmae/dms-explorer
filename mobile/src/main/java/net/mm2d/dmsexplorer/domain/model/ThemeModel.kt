/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.app.Activity

import androidx.annotation.ColorInt

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface ThemeModel {
    fun setThemeColor(
        activity: Activity,
        @ColorInt toolbarColor: Int,
        @ColorInt statusBarColor: Int
    )

    @ColorInt
    fun getToolbarColor(activity: Activity): Int
}
