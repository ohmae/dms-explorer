/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import android.graphics.Bitmap
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.util.ColorUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ServerColorExtractorDark : ServerColorExtractor {
    override fun invoke(server: MediaServer, icon: Bitmap?) {
        if (server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            return
        }
        extract(server, icon)
    }

    override fun invokeAsync(server: MediaServer, icon: Bitmap?) {
        if (server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            return
        }
        if (icon == null) {
            extractFromPalette(server, null)
        } else {
            androidx.palette.graphics.Palette.Builder(icon)
                .generate { palette -> extractFromPalette(server, palette) }
        }
    }

    private fun extract(server: MediaServer, icon: Bitmap?) {
        val palette = icon?.let { androidx.palette.graphics.Palette.Builder(it).generate() }
        extractFromPalette(server, palette)
    }

    private fun extractFromPalette(
        server: MediaServer,
        palette: androidx.palette.graphics.Palette?
    ) {
        val friendlyName = server.friendlyName
        var expandedColor = GENERATOR.getExpandedToolbarColor(friendlyName)
        var collapsedColor = GENERATOR.getControlColor(friendlyName)
        palette?.apply {
            PaletteUtils.selectLightSwatch(this)?.let {
                expandedColor = ColorUtils.getDarkerColor(it.rgb, 0.3f)
            }
            PaletteUtils.selectDarkSwatch(this)?.let {
                collapsedColor = ColorUtils.getDarkerColor(it.rgb, 0.3f)
            }
        }
        server.putIntTag(Const.KEY_TOOLBAR_EXPANDED_COLOR, expandedColor)
        server.putIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, collapsedColor)
        server.putBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, true)
    }

    companion object {
        private val GENERATOR = ThemeColorGeneratorDark()
    }
}
