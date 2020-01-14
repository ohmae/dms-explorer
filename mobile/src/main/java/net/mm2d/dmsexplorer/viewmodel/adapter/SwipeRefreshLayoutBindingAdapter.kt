/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter

import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object SwipeRefreshLayoutBindingAdapter {
    @BindingAdapter("colorSchemeResources")
    @JvmStatic
    fun setColorSchemeResources(
        view: SwipeRefreshLayout,
        colorResIds: IntArray?
    ) {
        colorResIds ?: return
        view.setColorSchemeResources(*colorResIds)
    }
}
