/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object DrawableUtils {
    @JvmStatic
    fun get(context: Context, @DrawableRes resId: Int): Drawable? {
        return AppCompatResources.getDrawable(context, resId)
    }
}
