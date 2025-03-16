/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model.control

import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnInfoListener
import android.media.MediaPlayer.OnPreparedListener

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MediaPlayerControl(
    private val mediaPlayer: MediaPlayer,
) : MediaControl {
    override val currentPosition: Int
        get() = try {
            mediaPlayer.currentPosition
        } catch (ignored: IllegalStateException) {
            0
        }
    override val duration: Int
        get() = try {
            mediaPlayer.duration
        } catch (ignored: IllegalStateException) {
            0
        }
    override val isPlaying: Boolean
        get() = try {
            mediaPlayer.isPlaying
        } catch (ignored: IllegalStateException) {
            false
        }

    override fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    override fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun seekTo(
        position: Int,
    ) {
        mediaPlayer.seekTo(position)
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    override fun stop() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    override fun setOnPreparedListener(
        listener: OnPreparedListener?,
    ) {
        mediaPlayer.setOnPreparedListener(listener)
    }

    override fun setOnErrorListener(
        listener: OnErrorListener?,
    ) {
        mediaPlayer.setOnErrorListener(listener)
    }

    override fun setOnInfoListener(
        listener: OnInfoListener?,
    ) {
        mediaPlayer.setOnInfoListener(listener)
    }

    override fun setOnCompletionListener(
        listener: OnCompletionListener?,
    ) {
        mediaPlayer.setOnCompletionListener(listener)
    }
}
