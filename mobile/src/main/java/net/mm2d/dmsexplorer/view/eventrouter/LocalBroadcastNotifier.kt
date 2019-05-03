/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class LocalBroadcastNotifier(
    private val broadcastManager: LocalBroadcastManager,
    private val action: String
) : EventNotifier {
    override fun send() {
        broadcastManager.sendBroadcast(Intent(action))
    }
}
