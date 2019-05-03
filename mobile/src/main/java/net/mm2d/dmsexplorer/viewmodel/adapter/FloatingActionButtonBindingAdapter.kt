/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object FloatingActionButtonBindingAdapter {
    @BindingAdapter("show")
    @JvmStatic
    fun setShow(
        view: FloatingActionButton,
        show: Boolean
    ) {
        if (show) {
            view.show()
        } else {
            view.hide()
        }
    }

    @BindingAdapter("backgroundTint")
    @JvmStatic
    fun setBackgroundTint(
        view: FloatingActionButton,
        @ColorInt color: Int
    ) {
        view.backgroundTintList = ColorStateList.valueOf(color)
    }
}
