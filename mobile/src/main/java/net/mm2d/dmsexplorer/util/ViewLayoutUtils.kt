/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import android.support.annotation.Px
import android.view.View
import android.view.ViewGroup.MarginLayoutParams

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ViewLayoutUtils {
    @JvmStatic
    fun setLayoutMarginRight(view: View, @Px margin: Int) {
        val params = view.layoutParams as? MarginLayoutParams ?: return
        params.rightMargin = margin
        view.layoutParams = params
    }

    @JvmStatic
    fun setPaddingBottom(view: View, @Px padding: Int) {
        view.updatePadding(bottom = padding)
    }

    private fun View.updatePadding(
        @Px left: Int = paddingLeft,
        @Px top: Int = paddingTop,
        @Px right: Int = paddingRight,
        @Px bottom: Int = paddingBottom
    ) {
        setPadding(left, top, right, bottom)
    }
}
