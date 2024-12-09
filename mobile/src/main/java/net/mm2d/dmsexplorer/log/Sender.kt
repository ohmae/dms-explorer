/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log

import android.os.Bundle
import androidx.annotation.Size

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal interface Sender {
    fun logEvent(
        @Size(min = 1L, max = 40L) name: String,
        params: Bundle?,
    )
}
