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

    @JvmStatic
    fun initialize(context: Context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
    }

    @JvmStatic
    fun createFinishNotifier(): EventNotifier {
        return LocalBroadcastNotifier(localBroadcastManager, ACTION_FINISH)
    }

    @JvmStatic
    fun createFinishObserver(): EventObserver {
        return LocalBroadcastObserver(localBroadcastManager, ACTION_FINISH)
    }

    @JvmStatic
    fun createOrientationSettingsNotifier(): EventNotifier {
        return LocalBroadcastNotifier(localBroadcastManager, ACTION_ORIENTATION_SETTINGS)
    }

    @JvmStatic
    fun createOrientationSettingsObserver(): EventObserver {
        return LocalBroadcastObserver(localBroadcastManager, ACTION_ORIENTATION_SETTINGS, true)
    }

    @JvmStatic
    fun createUpdateAvailabilityNotifier(): EventNotifier {
        return LocalBroadcastNotifier(localBroadcastManager, ACTION_UPDATE_AVAILABILITY)
    }

    @JvmStatic
    fun createUpdateAvailabilityObserver(): EventObserver {
        return LocalBroadcastObserver(localBroadcastManager, ACTION_UPDATE_AVAILABILITY)
    }
}
