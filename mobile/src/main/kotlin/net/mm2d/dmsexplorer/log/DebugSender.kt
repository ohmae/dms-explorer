/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log

import android.os.Bundle
import androidx.annotation.Size
import net.mm2d.log.Logger

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class DebugSender : Sender {

    override fun logEvent(
        @Size(min = 1L, max = 40L) name: String,
        params: Bundle?,
    ) {
        Logger.i { "\nname: " + name + "\nparams: " + dumpBundle(params) }
    }

    private fun dumpBundle(
        params: Bundle?,
    ): String {
        if (params == null) {
            return "null"
        }
        if (params.isEmpty) {
            return "empty"
        }
        val sb = StringBuilder()
        for (key in params.keySet()) {
            sb.append("\n\t")
            sb.append("key:")
            sb.append(key)
            sb.append(" value:")
            sb.append(params.get(key))
        }
        return sb.toString()
    }
}
