/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.tabs

import android.app.Activity
import android.os.Bundle
import net.mm2d.android.util.ActivityLifecycleCallbacksAdapter

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsBinder(
    private val session: CustomTabsHelper,
) : ActivityLifecycleCallbacksAdapter() {
    private var createdCount: Int = 0

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
        createdCount++
        session.bind()
    }

    override fun onActivityDestroyed(activity: Activity) {
        createdCount--
        if (createdCount == 0 && activity.isFinishing) {
            session.unbind()
        }
    }
}
