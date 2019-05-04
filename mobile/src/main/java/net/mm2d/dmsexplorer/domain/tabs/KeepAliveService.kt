/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.tabs

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class KeepAliveService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return sBinder
    }

    companion object {
        private val sBinder = Binder()
    }
}
