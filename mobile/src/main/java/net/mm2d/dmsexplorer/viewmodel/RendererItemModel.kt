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
import net.mm2d.android.upnp.avt.MediaRenderer
import net.mm2d.android.util.DrawableUtils
import net.mm2d.android.util.toDisplayableString
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class RendererItemModel(
    context: Context,
    renderer: MediaRenderer
) {
    var accentBackground: Drawable?
    var accentText: String?
    var accentIcon: Bitmap?
    val title: String
    val description: String

    init {
        val name = renderer.friendlyName
        title = name
        description = makeDescription(renderer)
        val binary = renderer.getIcon()?.binary
        if (binary == null) {
            accentIcon = null
            accentText = if (name.isEmpty()) "" else name.substring(0, 1).toDisplayableString()
            val generator = Settings.get()
                .themeParams
                .themeColorGenerator
            accentBackground = DrawableUtils.get(context, R.drawable.ic_circle)?.also {
                it.mutate()
                DrawableCompat.setTint(it, generator.getIconColor(name))
            }
        } else {
            accentIcon = BitmapFactory.decodeByteArray(binary, 0, binary.size)
            accentText = null
            accentBackground = null
        }
    }

    private fun makeDescription(renderer: MediaRenderer): String {
        val sb = StringBuilder()
        val manufacture = renderer.manufacture
        if (!manufacture.isNullOrEmpty()) {
            sb.append(manufacture)
            sb.append('\n')
        }
        sb.append("IP: ")
        sb.append(renderer.ipAddress)
        val serial = renderer.serialNumber
        if (!serial.isNullOrEmpty()) {
            sb.append("  Serial: ")
            sb.append(serial)
        }
        return sb.toString()
    }
}
