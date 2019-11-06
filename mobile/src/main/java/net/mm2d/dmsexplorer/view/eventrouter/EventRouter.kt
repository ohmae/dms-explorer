/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter

import net.mm2d.dmsexplorer.view.eventrouter.Event.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventRouter {
    fun notifyFinish() {
        EventBus.notify(EVENT_FINISH)
    }

    fun notifyOrientationSettings() {
        EventBus.notify(EVENT_ORIENTATION_SETTINGS)
    }

    fun notifyUpdateAvailabilityNotifier() {
        EventBus.notify(EVENT_UPDATE_AVAILABILITY)
    }

    fun createFinishObserver(): EventObserver =
        RxEventObserver(EVENT_FINISH)

    fun createOrientationSettingsObserver(): EventObserver =
        RxEventObserver(EVENT_ORIENTATION_SETTINGS, true)

    fun createUpdateAvailabilityObserver(): EventObserver =
        RxEventObserver(EVENT_UPDATE_AVAILABILITY)
}
