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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventRouter {
    private val finishChannel: MutableSharedFlow<Unit> = MutableSharedFlow()
    private val orientationSettingsChannel: MutableSharedFlow<Unit> = MutableSharedFlow(1)
    private val updateAvailableLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun notifyFinish() {
        scope.launch {
            finishChannel.emit(Unit)
        }
    }

    fun observeFinish(
        owner: LifecycleOwner,
        callback: () -> Unit,
    ) {
        owner.lifecycleScope.launch {
            finishChannel.collect { callback() }
        }
    }

    fun notifyOrientationSettings() {
        scope.launch {
            orientationSettingsChannel.emit(Unit)
        }
    }

    fun observeOrientationSettings(
        owner: LifecycleOwner,
        callback: () -> Unit,
    ) {
        owner.lifecycleScope.launch {
            orientationSettingsChannel.collect { callback() }
        }
    }

    fun updateAvailable(): Boolean = updateAvailableLiveData.value == true

    fun notifyUpdateAvailable(
        available: Boolean,
    ) {
        updateAvailableLiveData.postValue(available)
    }

    fun observeUpdateAvailable(
        owner: LifecycleOwner,
        callback: (Boolean) -> Unit,
    ) {
        updateAvailableLiveData.observe(owner) { callback(it) }
    }
}
