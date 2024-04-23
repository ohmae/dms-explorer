/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object RecyclerViewBindingAdapter {
    fun setScrollPosition(
        view: RecyclerView,
        position: Int,
    ) {
        if (position < 0) {
            return
        }
        view.scrollToPosition(position)
        view.post { scrollToCenter(view, position) }
    }

    private fun scrollToCenter(
        view: RecyclerView,
        position: Int,
    ) {
        val child = findPositionView(view, position) ?: return
        val dy = (child.top - (view.height - child.height) / 2f).toInt()
        view.smoothScrollBy(0, dy)
    }

    private fun findPositionView(
        view: RecyclerView,
        position: Int,
    ): View? {
        val child = view.getChildAt(0) ?: return null
        val top = view.getChildAdapterPosition(child)
        return if (top < 0) null else view.getChildAt(position - top)
    }
}
