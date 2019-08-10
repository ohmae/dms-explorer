/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log

import android.content.Context

import net.mm2d.dmsexplorer.BuildConfig

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object SenderFactory {
    fun create(context: Context): Sender = if (BuildConfig.DEBUG) DebugSender() else EmptySender()
}
