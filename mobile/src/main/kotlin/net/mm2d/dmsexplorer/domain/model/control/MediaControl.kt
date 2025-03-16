/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model.control

import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnInfoListener
import android.media.MediaPlayer.OnPreparedListener

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface MediaControl {
    val currentPosition: Int
    val duration: Int
    val isPlaying: Boolean
    fun play()
    fun pause()
    fun seekTo(
        position: Int,
    )
    fun stop()
    fun setOnPreparedListener(
        listener: OnPreparedListener?,
    )

    fun setOnErrorListener(
        listener: OnErrorListener?,
    )

    fun setOnInfoListener(
        listener: OnInfoListener?,
    )

    fun setOnCompletionListener(
        listener: OnCompletionListener?,
    )
}
