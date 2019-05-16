/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.DrawableCompat
import net.mm2d.android.util.DrawableUtils
import net.mm2d.android.util.toDisplayableString
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.entity.ContentType
import net.mm2d.dmsexplorer.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentItemModel(
    context: Context,
    entity: ContentEntity,
    val selected: Boolean
) {
    val accentBackground: Drawable
    val accentText: String
    val title: String
    val description: String
    val hasDescription: Boolean
    @DrawableRes
    val imageResource: Int
    val isProtected: Boolean

    init {
        val name = entity.name
        accentText = if (name.isEmpty()) "" else name.substring(0, 1).toDisplayableString()
        val generator = Settings.get()
            .themeParams
            .themeColorGenerator
        accentBackground = DrawableUtils.get(context, R.drawable.ic_circle)!!.also {
            it.mutate()
            DrawableCompat.setTint(it, generator.getIconColor(name))
        }
        title = name.toDisplayableString()
        description = entity.description
        hasDescription = description.isNotEmpty()
        imageResource = getImageResource(entity)
        isProtected = entity.isProtected
    }

    @DrawableRes
    private fun getImageResource(entity: ContentEntity): Int {
        return when (entity.type) {
            ContentType.CONTAINER -> R.drawable.ic_folder
            ContentType.MOVIE -> R.drawable.ic_movie
            ContentType.MUSIC -> R.drawable.ic_music
            ContentType.PHOTO -> R.drawable.ic_image
            else -> 0
        }
    }
}
