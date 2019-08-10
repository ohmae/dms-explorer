/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ViewPagerAdapter : PagerAdapter() {
    private val viewList: MutableList<View> = ArrayList()

    fun add(view: View) {
        viewList.add(view)
    }

    operator fun get(position: Int): View = viewList[position]

    fun clear() {
        viewList.clear()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        viewList[position].also {
            container.addView(it)
        }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun getCount(): Int = viewList.size

    override fun isViewFromObject(view: View, any: Any): Boolean = view === any
}
