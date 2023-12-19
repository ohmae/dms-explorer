/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.android.util.DrawableUtils
import net.mm2d.android.util.toDisplayableString
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerItemModel(
    context: Context,
    server: MediaServer,
    val selected: Boolean,
) {
    var accentBackground: Drawable?
    var accentText: String?
    var accentIcon: Bitmap?
    val title: String
    val description: String

    init {
        val name = server.friendlyName
        title = name
        description = makeDescription(server)
        val params = Settings.get().themeParams
        val binary = server.getIcon()?.binary
        if (binary == null) {
            accentIcon = null
            accentText = if (name.isEmpty()) "" else name.substring(0, 1).toDisplayableString()
            accentBackground = DrawableUtils.get(context, R.drawable.ic_circle)?.also {
                it.mutate()
                DrawableCompat.setTint(it, params.themeColorGenerator.getIconColor(name))
            }
            params.serverColorExtractor.invokeAsync(server, null)
        } else {
            accentIcon = BitmapFactory.decodeByteArray(binary, 0, binary.size)
            accentText = null
            accentBackground = null
            params.serverColorExtractor.invokeAsync(server, accentIcon)
        }
    }

    private fun makeDescription(server: MediaServer): String {
        val sb = StringBuilder()
        val manufacture = server.manufacture
        if (!manufacture.isNullOrEmpty()) {
            sb.append(manufacture)
            sb.append('\n')
        }
        sb.append("IP: ")
        sb.append(server.ipAddress)
        val serial = server.serialNumber
        if (!serial.isNullOrEmpty()) {
            sb.append("  Serial: ")
            sb.append(serial)
        }
        return sb.toString()
    }
}
