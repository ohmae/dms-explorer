/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal open class LocalBroadcastObserver(
    private val broadcastManager: LocalBroadcastManager,
    private val action: String,
    private val callbackOnRegister: Boolean = false
) : EventObserver {
    private var callback: (() -> Unit)? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            callback?.invoke()
        }
    }

    override fun register(callback: () -> Unit) {
        if (callbackOnRegister) {
            callback.invoke()
        }
        this.callback = callback
        broadcastManager.registerReceiver(broadcastReceiver, IntentFilter(action))
    }

    override fun unregister() {
        broadcastManager.unregisterReceiver(broadcastReceiver)
    }
}
