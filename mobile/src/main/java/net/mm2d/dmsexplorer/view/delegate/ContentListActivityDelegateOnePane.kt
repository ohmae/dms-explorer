/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate

import android.view.View
import com.google.android.material.snackbar.Snackbar
import net.mm2d.android.util.ActivityUtils
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.util.ItemSelectUtils
import net.mm2d.dmsexplorer.view.ContentDetailActivity
import net.mm2d.dmsexplorer.view.base.BaseActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ContentListActivityDelegateOnePane(
    activity: BaseActivity,
    binding: ContentListActivityBinding,
) : ContentListActivityDelegate(activity, binding) {

    override val isTwoPane: Boolean = false

    override fun onSelect(
        v: View,
        entity: ContentEntity,
    ) {
        startDetailActivity(v)
    }

    override fun onLostSelection() {}

    override fun onExecute(v: View, entity: ContentEntity, selected: Boolean) {
        if (entity.isProtected) {
            Snackbar.make(v, R.string.toast_not_support_drm, Snackbar.LENGTH_LONG).show()
            return
        }
        ItemSelectUtils.play(activity, 0)
    }

    private fun startDetailActivity(v: View) {
        val intent = ContentDetailActivity.makeIntent(activity)
        activity.startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(v))
    }

    override fun onDelete() {}
}
