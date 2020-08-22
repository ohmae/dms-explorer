/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventRouter {
    private val finishChannel = BroadcastChannel<Unit>(1)
    private val orientationSettingsChannel = BroadcastChannel<Unit>(1)
    private val updateAvailabilityChannel = BroadcastChannel<Unit>(1)

    fun notifyFinish() {
        GlobalScope.launch {
            finishChannel.send(Unit)
        }
    }

    fun observeFinish(owner: LifecycleOwner, callback: () -> Unit) {
        owner.lifecycleScope.launch {
            finishChannel.asFlow().collect { callback() }
        }
    }

    fun notifyOrientationSettings() {
        GlobalScope.launch {
            orientationSettingsChannel.send(Unit)
        }
    }

    fun observeOrientationSettings(owner: LifecycleOwner, callback: () -> Unit) {
        owner.lifecycleScope.launch {
            orientationSettingsChannel.asFlow().collect { callback() }
        }
    }

    fun notifyUpdateAvailabilityNotifier() {
        GlobalScope.launch {
            updateAvailabilityChannel.send(Unit)
        }
    }

    fun observeUpdateAvailability(owner: LifecycleOwner, callback: () -> Unit) {
        owner.lifecycleScope.launch {
            updateAvailabilityChannel.asFlow().collect { callback() }
        }
    }
}
