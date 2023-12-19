/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter

import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import net.mm2d.dmsexplorer.util.AttrUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object TextViewBindingAdapter {
    @BindingAdapter("underline")
    @JvmStatic
    fun setUnderlineFlag(
        view: TextView,
        underline: Boolean,
    ) {
        view.paintFlags = if (underline) {
            view.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            view.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
    }

    @BindingAdapter("textColorAttr")
    @JvmStatic
    fun setTextColorAttr(
        view: TextView,
        attr: Int,
    ) {
        view.setTextColor(AttrUtils.resolveColor(view.context, attr, Color.BLACK))
    }
}
