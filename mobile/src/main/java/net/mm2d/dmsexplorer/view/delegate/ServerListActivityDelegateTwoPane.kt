/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate

import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.transition.Slide
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding
import net.mm2d.dmsexplorer.view.ServerDetailFragment
import net.mm2d.dmsexplorer.view.base.BaseActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ServerListActivityDelegateTwoPane(
    activity: BaseActivity,
    binding: ServerListActivityBinding,
) : ServerListActivityDelegate(activity, binding) {
    private var fragment: Fragment? = null

    override val isTwoPane: Boolean
        get() = true

    override fun onSelect(v: View) {
        setDetailFragment(true)
    }

    override fun onLostSelection() {
        removeDetailFragment()
    }

    override fun onExecute(v: View) {
        startCdsListActivity(activity, v)
    }

    override fun prepareSaveInstanceState() {
        removeDetailFragment()
    }

    override fun onStart() {
        model.updateListAdapter()
        updateFragmentState()
    }

    private fun setDetailFragment(animate: Boolean) {
        val fragment = ServerDetailFragment.newInstance()
        this.fragment = fragment
        if (animate) {
            val transition = Slide(Gravity.BOTTOM)
                .setDuration(150L)
                .setInterpolator(DecelerateInterpolator())
            fragment.enterTransition = transition
        }
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.server_detail_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun removeDetailFragment() {
        fragment?.let {
            activity.supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commitAllowingStateLoss()
        }
        fragment = null
    }

    private fun updateFragmentState() {
        if (model.hasSelectedMediaServer()) {
            setDetailFragment(false)
            return
        }
        removeDetailFragment()
    }
}
