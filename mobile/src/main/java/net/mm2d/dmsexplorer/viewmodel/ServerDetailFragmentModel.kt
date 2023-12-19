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
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.android.util.ActivityUtils
import net.mm2d.android.util.DrawableUtils
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.ContentListActivity
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter
import net.mm2d.upnp.Icon

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerDetailFragmentModel(
    private val context: Context,
    repository: Repository,
) {
    val collapsedColor: Int
    val expandedColor: Int
    val icon: Drawable
    val title: String
    val propertyAdapter: PropertyAdapter

    init {
        val server = repository.controlPointModel.selectedMediaServer
            ?: throw IllegalStateException()
        title = server.friendlyName
        propertyAdapter = PropertyAdapter.ofServer(context, server)
        val iconBitmap = createIconBitmap(server.getIcon())
        icon = createIconDrawable(context, server, iconBitmap)

        Settings.get()
            .themeParams
            .serverColorExtractor
            .invoke(server, iconBitmap)
        expandedColor = server.getIntTag(Const.KEY_TOOLBAR_EXPANDED_COLOR, Color.BLACK)
        collapsedColor = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK)
    }

    private fun createIconBitmap(icon: Icon?): Bitmap? {
        val binary = icon?.binary ?: return null
        return BitmapFactory.decodeByteArray(binary, 0, binary.size)
    }

    private fun createIconDrawable(
        context: Context,
        server: MediaServer,
        icon: Bitmap?,
    ): Drawable {
        if (icon != null) {
            return BitmapDrawable(context.resources, icon)
        }
        val generator = Settings.get()
            .themeParams
            .themeColorGenerator
        return DrawableUtils.getOrThrow(context, R.drawable.ic_circle).also {
            it.mutate()
            DrawableCompat.setTint(it, generator.getIconColor(server.friendlyName))
        }
    }

    fun onClickFab(view: View) {
        val intent = ContentListActivity.makeIntent(context)
        context.startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(view))
        EventLogger.sendSelectServer()
    }
}
