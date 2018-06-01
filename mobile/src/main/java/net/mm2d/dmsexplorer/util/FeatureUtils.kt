/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import android.content.Context
import android.content.pm.PackageManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object FeatureUtils {
    @JvmStatic
    fun hasTouchScreen(context: Context): Boolean {
        return context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }
}
