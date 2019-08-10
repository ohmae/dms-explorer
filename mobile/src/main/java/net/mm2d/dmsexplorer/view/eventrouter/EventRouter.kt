/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventRouter {
    private const val ACTION_UPDATE_AVAILABILITY = "ACTION_UPDATE_AVAILABILITY"
    private const val ACTION_ORIENTATION_SETTINGS = "ACTION_ORIENTATION_SETTINGS"
    private const val ACTION_FINISH = "ACTION_FINISH"

    private lateinit var localBroadcastManager: LocalBroadcastManager

    fun initialize(context: Context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
    }

    fun createFinishNotifier(): EventNotifier =
        LocalBroadcastNotifier(localBroadcastManager, ACTION_FINISH)

    fun createFinishObserver(): EventObserver =
        LocalBroadcastObserver(localBroadcastManager, ACTION_FINISH)

    fun createOrientationSettingsNotifier(): EventNotifier =
        LocalBroadcastNotifier(localBroadcastManager, ACTION_ORIENTATION_SETTINGS)

    fun createOrientationSettingsObserver(): EventObserver =
        LocalBroadcastObserver(localBroadcastManager, ACTION_ORIENTATION_SETTINGS, true)

    fun createUpdateAvailabilityNotifier(): EventNotifier =
        LocalBroadcastNotifier(localBroadcastManager, ACTION_UPDATE_AVAILABILITY)

    fun createUpdateAvailabilityObserver(): EventObserver =
        LocalBroadcastObserver(localBroadcastManager, ACTION_UPDATE_AVAILABILITY)
}
