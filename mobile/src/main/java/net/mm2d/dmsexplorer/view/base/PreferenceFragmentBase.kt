/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.dmsexplorer.util.ViewLayoutUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class PreferenceFragmentBase : PreferenceFragmentCompat() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        listView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        ViewLayoutUtils.setPaddingBottom(container!!, 0)
        return view
    }
}
