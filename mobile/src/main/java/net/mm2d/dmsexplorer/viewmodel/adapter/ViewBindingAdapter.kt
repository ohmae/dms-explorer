/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter

import android.view.View
import androidx.databinding.BindingAdapter
import net.mm2d.dmsexplorer.util.ViewLayoutUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ViewBindingAdapter {
    @BindingAdapter("layout_marginRight")
    @JvmStatic
    fun setLayoutMarginRight(
        view: View,
        margin: Int,
    ) {
        ViewLayoutUtils.setLayoutMarginRight(view, margin)
    }
}
