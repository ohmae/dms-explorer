/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper

import android.content.Context
import android.media.AudioManager

import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MuteAlertHelper(
    private val context: Context
) {
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun alertIfMuted() {
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toaster.show(context, R.string.toast_currently_muted)
        }
    }
}
