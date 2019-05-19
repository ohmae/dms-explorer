/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import android.app.Activity
import android.view.View

import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.view.IntroductoryOverlay

import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object RepeatIntroductionUtils {
    val TIMEOUT = TimeUnit.SECONDS.toMillis(8)

    fun show(activity: Activity, view: View): Boolean {
        val settings = Settings.get()
        if (settings.isRepeatIntroduced) {
            return false
        }
        settings.notifyRepeatIntroduced()
        IntroductoryOverlay.Builder(activity)
            .setView(view)
            .setTitleText(R.string.repeat_introduction_title)
            .setSubtitleText(R.string.repeat_introduction_subtitle)
            .setOverlayColor(R.color.overlayIntroductory)
            .setDimmerColor(R.color.dimmerIntroductory)
            .setTimeout(TIMEOUT)
            .build()
            .show()
        return true
    }
}
