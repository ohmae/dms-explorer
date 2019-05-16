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
import com.google.android.material.snackbar.Snackbar
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.util.ItemSelectUtils
import net.mm2d.dmsexplorer.view.ContentDetailFragment
import net.mm2d.dmsexplorer.view.base.BaseActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ContentListActivityDelegateTwoPane(
    activity: BaseActivity,
    binding: ContentListActivityBinding
) : ContentListActivityDelegate(activity, binding) {
    private var fragment: Fragment? = null
    override val isTwoPane: Boolean = true

    override fun onSelect(
        v: View,
        entity: ContentEntity
    ) {
        setDetailFragment(true)
    }

    override fun onLostSelection() {
        removeDetailFragment()
    }

    override fun onExecute(
        v: View,
        entity: ContentEntity,
        selected: Boolean
    ) {
        if (entity.isProtected) {
            if (!selected) {
                setDetailFragment(true)
            }
            Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show()
            return
        }
        ItemSelectUtils.play(activity, 0)
    }

    private fun setDetailFragment(animate: Boolean) {
        val fragment = ContentDetailFragment.newInstance()
        this.fragment = fragment
        if (animate) {
            val transition = Slide(Gravity.BOTTOM)
                .setDuration(150L)
                .setInterpolator(DecelerateInterpolator())
            fragment.enterTransition = transition
        }
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.cds_detail_container, fragment)
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

    override fun onStart() {
        super.onStart()
        if (model?.isItemSelected == true) {
            setDetailFragment(false)
        }
    }

    override fun onDelete() {
        removeDetailFragment()
    }
}
