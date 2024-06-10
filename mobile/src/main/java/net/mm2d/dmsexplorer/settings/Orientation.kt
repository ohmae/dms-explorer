/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.annotation.StringRes
import net.mm2d.dmsexplorer.R
import net.mm2d.log.Logger

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
enum class Orientation(
    private val value: Int,
    @StringRes
    private val nameId: Int,
) {
    UNSPECIFIED(
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
        R.string.orientation_unspecified,
    ),
    PORTRAIT(
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
        R.string.orientation_portrait,
    ),
    LANDSCAPE(
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
        R.string.orientation_landscape,
    ),
    REVERSE_PORTRAIT(
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
        R.string.orientation_reverse_portrait,
    ),
    REVERSE_LANDSCAPE(
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        R.string.orientation_reverse_landscape,
    ),
    ;

    fun getName(context: Context): String = context.getString(nameId)

    fun setRequestedOrientation(activity: Activity) {
        Log.e("XXXX", "setRequestedOrientation: $activity $value")
        try {
            activity.requestedOrientation = value
        } catch (e: Exception) {
            Logger.d(e)
        }
    }

    companion object {
        private val map = entries.map { it.name to it }.toMap()

        fun of(name: String): Orientation =
            map.getOrElse(name) { UNSPECIFIED }
    }
}
