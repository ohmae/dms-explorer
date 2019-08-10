/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper

import android.app.Activity
import android.os.Build

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object PipHelpers {
    private val MOCK = MovieActivityPipHelperEmpty()

    val isSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    fun getMovieHelper(activity: Activity): MovieActivityPipHelper =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MovieActivityPipHelperOreo(activity)
        } else MOCK
}
