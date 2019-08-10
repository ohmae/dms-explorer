/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object DrawableUtils {
    fun get(context: Context, @DrawableRes resId: Int): Drawable? =
        AppCompatResources.getDrawable(context, resId)

    fun getOrThrow(context: Context, @DrawableRes resId: Int): Drawable =
        AppCompatResources.getDrawable(context, resId)
            ?: throw IllegalArgumentException("resource not found")
}
