/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object Toaster {
    fun show(
        context: Context?,
        @StringRes resId: Int,
    ): Toast? =
        context?.let {
            Toast.makeText(it, resId, Toast.LENGTH_LONG).apply { show() }
        }
}
