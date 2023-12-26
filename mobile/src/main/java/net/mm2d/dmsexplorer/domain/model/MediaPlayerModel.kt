/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener
import net.mm2d.dmsexplorer.domain.model.control.MediaControl
import net.mm2d.log.Logger
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class MediaPlayerModel(
    private val mediaControl: MediaControl,
) : PlayerModel {
    private val handler = Handler(Looper.getMainLooper())
    private var statusListener: StatusListener = STATUS_LISTENER
    final override var isPlaying: Boolean = false
        private set(playing) {
            if (isPlaying != playing) {
                field = playing
                statusListener.notifyPlayingState(playing)
            }
        }
    final override var duration: Int = 0
        private set(duration) {
            if (this.duration != duration) {
                field = duration
                statusListener.notifyDuration(duration)
            }
        }
    private var terminated: Boolean = false

    private val getPositionTask = object : Runnable {
        override fun run() {
            try {
                var sleep = 1000
                val playing = mediaControl.isPlaying
                isPlaying = playing
                if (playing) {
                    val duration = mediaControl.duration
                    val position = mediaControl.currentPosition
                    if (duration >= position) {
                        progress = position
                        this@MediaPlayerModel.duration = duration
                    }
                    sleep = 1001 - position % 1000
                }
                sleep = sleep.coerceIn(100, 1000)
                handler.removeCallbacks(this)
                handler.postDelayed(this, sleep.toLong())
            } catch (ignored: IllegalStateException) {
            }
        }
    }

    private var _progress: Int = 0
    final override var progress: Int
        get() = _progress
        private set(progress) {
            if (_progress != progress) {
                _progress = progress
                statusListener.notifyProgress(progress)
            }
        }

    init {
        mediaControl.setOnPreparedListener { onPrepared(it) }
    }

    override fun canPause(): Boolean = true

    @CallSuper
    override fun terminate() {
        if (terminated) {
            return
        }
        mediaControl.setOnPreparedListener(null)
        mediaControl.setOnErrorListener(null)
        mediaControl.setOnInfoListener(null)
        mediaControl.setOnCompletionListener(null)
        mediaControl.stop()
        _progress = 0
        terminated = true
    }

    override fun setStatusListener(listener: StatusListener) {
        statusListener = listener
        mediaControl.setOnErrorListener { _, what, extra ->
            logError(what, extra)
            statusListener.onError(what, extra)
        }
        mediaControl.setOnInfoListener { _, what, extra ->
            logInfo(what, extra)
            statusListener.onInfo(what, extra)
        }
        mediaControl.setOnCompletionListener(listener = {
            handler.removeCallbacks(getPositionTask)
            statusListener.onCompletion()
        })
    }

    override fun restoreSaveProgress(progress: Int) {
        _progress = progress
    }

    override fun play() {
        mediaControl.play()
    }

    override fun pause() {
        mediaControl.pause()
    }

    override fun seekTo(position: Int) {
        mediaControl.seekTo(position)
        handler.removeCallbacks(getPositionTask)
        handler.post(getPositionTask)
    }

    override operator fun next(): Boolean = false

    override fun previous(): Boolean {
        if (mediaControl.currentPosition < SKIP_MARGIN) {
            return false
        }
        seekTo(0)
        return true
    }

    protected abstract fun preparePlaying(mediaPlayer: MediaPlayer)

    @CallSuper
    fun onPrepared(mediaPlayer: MediaPlayer) {
        preparePlaying(mediaPlayer)
        duration = duration
        handler.post(getPositionTask)
        play()
        if (_progress > 0) {
            seekTo(_progress)
        }
        isPlaying = isPlaying
    }

    private fun logError(what: Int, extra: Int) {
        Logger.e { "onError:w $what ${getErrorWhatString(what)} e $extra ${getErrorExtraString(extra)}" }
    }

    private fun logInfo(what: Int, extra: Int) {
        Logger.d { "onInfo:w $what ${getInfoWhatString(what)} e $extra" }
    }

    private fun getErrorWhatString(what: Int): String = when (what) {
        MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "MEDIA_ERROR_SERVER_DIED"
        MediaPlayer.MEDIA_ERROR_UNKNOWN -> "MEDIA_ERROR_UNKNOWN"
        else -> ""
    }

    private fun getErrorExtraString(extra: Int): String = when (extra) {
        MediaPlayer.MEDIA_ERROR_IO -> "MEDIA_ERROR_IO"
        MediaPlayer.MEDIA_ERROR_MALFORMED -> "MEDIA_ERROR_MALFORMED"
        MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "MEDIA_ERROR_TIMED_OUT"
        MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "MEDIA_ERROR_UNSUPPORTED"
        MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK"
        MEDIA_ERROR_SYSTEM -> "MEDIA_ERROR_SYSTEM"
        else -> ""
    }

    private fun getInfoWhatString(what: Int): String = when (what) {
        MediaPlayer.MEDIA_INFO_UNKNOWN -> "MEDIA_INFO_UNKNOWN"
        MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> "MEDIA_INFO_VIDEO_TRACK_LAGGING"
        MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> "MEDIA_INFO_VIDEO_RENDERING_START"
        MediaPlayer.MEDIA_INFO_BUFFERING_START -> "MEDIA_INFO_BUFFERING_START"
        MediaPlayer.MEDIA_INFO_BUFFERING_END -> "MEDIA_INFO_BUFFERING_END"
        MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING -> "MEDIA_INFO_BAD_INTERLEAVING"
        MediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> "MEDIA_INFO_NOT_SEEKABLE"
        MediaPlayer.MEDIA_INFO_METADATA_UPDATE -> "MEDIA_INFO_METADATA_UPDATE"
        MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE -> "MEDIA_INFO_UNSUPPORTED_SUBTITLE"
        MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT -> "MEDIA_INFO_SUBTITLE_TIMED_OUT"
        else -> ""
    }

    companion object {
        private val SKIP_MARGIN = TimeUnit.SECONDS.toMillis(3).toInt()
        private const val MEDIA_ERROR_SYSTEM = -2147483648
        private val STATUS_LISTENER = StatusListenerAdapter()
    }
}
