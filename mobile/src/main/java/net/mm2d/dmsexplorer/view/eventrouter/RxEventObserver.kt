/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal open class RxEventObserver(
    private val event: Event,
    private val callbackOnRegister: Boolean = false
) : EventObserver {
    private var disposable: Disposable? = null

    override fun register(callback: () -> Unit) {
        if (callbackOnRegister) {
            callback.invoke()
        }
        disposable?.dispose()
        disposable = EventBus.observable()
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it == event }
            .subscribe { callback.invoke() }
    }

    override fun unregister() {
        disposable?.dispose()
    }
}
