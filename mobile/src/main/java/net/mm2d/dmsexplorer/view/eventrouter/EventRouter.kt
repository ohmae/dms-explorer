/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
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
    private val finishChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private val orientationSettingsChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private val updateAvailableLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

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

    fun updateAvailable(): Boolean =
        updateAvailableLiveData.value == true

    fun notifyUpdateAvailability(available: Boolean) {
        updateAvailableLiveData.postValue(available)
    }

    fun observeUpdateAvailability(owner: LifecycleOwner, callback: (Boolean) -> Unit) {
        updateAvailableLiveData.observe(owner, { callback(it) })
    }
}
