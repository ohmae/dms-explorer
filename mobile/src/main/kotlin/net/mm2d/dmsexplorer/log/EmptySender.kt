/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log

import android.os.Bundle

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class EmptySender internal constructor() : Sender {
    override fun logEvent(
        name: String,
        params: Bundle?,
    ) {
    }
}
