/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import android.graphics.Bitmap
import net.mm2d.android.upnp.cds.MediaServer

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface ServerColorExtractor {
    operator fun invoke(
        server: MediaServer,
        icon: Bitmap?,
    )

    fun invokeAsync(
        server: MediaServer,
        icon: Bitmap?,
    )
}
