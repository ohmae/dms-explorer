/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.domain.model.PlayerModel
import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener
import net.mm2d.dmsexplorer.settings.RepeatMode
import net.mm2d.dmsexplorer.view.view.ScrubBar
import net.mm2d.dmsexplorer.view.view.ScrubBar.Accuracy
import net.mm2d.dmsexplorer.view.view.ScrubBar.ScrubBarListener
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ControlPanelModel internal constructor(
    private val context: Context,
    private val playerModel: PlayerModel
) : BaseObservable(), StatusListener {
    private var repeatMode = RepeatMode.PLAY_ONCE
    private var error: Boolean = false
    var isSkipped: Boolean = false
        private set
    private var tracking: Boolean = false
    private var onCompletionListener: (() -> Unit)? = null
    private var onNextListener: (() -> Unit)? = null
    private var onPreviousListener: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    private val onCompletionTask = Runnable { onCompletion() }

    @get:Bindable
    var progressText = makeTimeText(0)
        private set

    @get:Bindable
    var durationText = makeTimeText(0)
        private set
    var isPlaying: Boolean = false
        private set(playing) {
            if (isPlaying == playing) {
                return
            }
            field = playing
            playButtonResId = if (playing) R.drawable.ic_pause else R.drawable.ic_play
        }

    @get:Bindable
    var isPrepared: Boolean = false
        private set(prepared) {
            field = prepared
            notifyPropertyChanged(BR.prepared)
        }

    @get:Bindable
    var duration: Int = 0
        private set(duration) {
            field = duration
            notifyPropertyChanged(BR.duration)
            if (duration > 0) {
                isSeekable = true
            }
            setDurationText(duration)
            isPrepared = true
        }

    @get:Bindable
    var progress: Int = 0
        private set(progress) {
            if (tracking) {
                return
            }
            setProgressText(progress)
            field = progress
            notifyPropertyChanged(BR.progress)
        }

    @get:Bindable
    var isSeekable: Boolean = false
        private set(seekable) {
            field = seekable
            notifyPropertyChanged(BR.seekable)
        }

    @get:Bindable
    var playButtonResId = R.drawable.ic_play
        private set(playButtonResId) {
            field = playButtonResId
            notifyPropertyChanged(BR.playButtonResId)
        }

    @get:Bindable
    var scrubText = ""
        private set(scrubText) {
            field = scrubText
            notifyPropertyChanged(BR.scrubText)
        }

    @get:Bindable
    var isNextEnabled: Boolean = false
        set(nextEnabled) {
            field = nextEnabled
            notifyPropertyChanged(BR.nextEnabled)
        }

    @get:Bindable
    var isPreviousEnabled: Boolean = false
        set(previousEnabled) {
            field = previousEnabled
            notifyPropertyChanged(BR.previousEnabled)
        }

    val seekBarListener: ScrubBarListener = object : ScrubBarListener {
        override fun onProgressChanged(
            seekBar: ScrubBar,
            progress: Int,
            fromUser: Boolean
        ) {
            if (fromUser) {
                setProgressText(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: ScrubBar) {
            tracking = true
        }

        override fun onStopTrackingTouch(seekBar: ScrubBar) {
            tracking = false
            isSkipped = true
            playerModel.seekTo(seekBar.progress)
            scrubText = ""
        }

        override fun onAccuracyChanged(
            seekBar: ScrubBar,
            @Accuracy accuracy: Int
        ) {
            scrubText = getScrubText(accuracy)
        }
    }

    init {
        playerModel.setStatusListener(this)
        isPreviousEnabled = true
    }

    internal fun terminate() {
        playerModel.terminate()
    }

    internal fun restoreSaveProgress(position: Int) {
        playerModel.restoreSaveProgress(position)
    }

    internal fun setOnCompletionListener(listener: (() -> Unit)?) {
        onCompletionListener = listener
    }

    internal fun setSkipControlListener(onNext: (() -> Unit)?, onPrevious: (() -> Unit)?) {
        onNextListener = onNext
        onPreviousListener = onPrevious
    }

    fun onClickPlayPause() {
        val playing = playerModel.isPlaying
        if (playing) {
            playerModel.pause()
        } else {
            playerModel.play()
        }
        isPlaying = !playing
    }

    fun onClickPlay() {
        val playing = playerModel.isPlaying
        if (!playing) {
            playerModel.play()
            isPlaying = true
        }
    }

    fun onClickPause() {
        onClickPlayPause()
    }

    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
        isNextEnabled = when (mode) {
            RepeatMode.PLAY_ONCE,
            RepeatMode.REPEAT_ONE -> false
            RepeatMode.SEQUENTIAL,
            RepeatMode.REPEAT_ALL -> true
        }
    }

    fun onClickNext() {
        if (!isNextEnabled) {
            return
        }
        if (!playerModel.next()) {
            onNextListener?.invoke()
        }
    }

    fun onClickPrevious() {
        if (!isPreviousEnabled) {
            return
        }
        if (!playerModel.previous()) {
            onPreviousListener?.invoke()
        }
    }

    private fun setProgressText(progress: Int) {
        progressText = makeTimeText(progress)
        notifyPropertyChanged(BR.progressText)
    }

    private fun setDurationText(duration: Int) {
        durationText = makeTimeText(duration)
        notifyPropertyChanged(BR.durationText)
    }

    private fun getScrubText(accuracy: Int): String {
        return when (accuracy) {
            ScrubBar.ACCURACY_NORMAL -> context.getString(R.string.seek_bar_scrub_normal)
            ScrubBar.ACCURACY_HALF -> context.getString(R.string.seek_bar_scrub_half)
            ScrubBar.ACCURACY_QUARTER -> context.getString(R.string.seek_bar_scrub_quarter)
            else -> ""
        }
    }

    override fun notifyDuration(duration: Int) {
        this.duration = duration
    }

    override fun notifyProgress(progress: Int) {
        this.progress = progress
    }

    override fun notifyPlayingState(playing: Boolean) {
        isPlaying = playing
    }

    override fun notifyChapterList(chapterList: List<Int>) {}

    override fun onError(
        what: Int,
        extra: Int
    ): Boolean {
        error = true
        Toaster.show(context, R.string.toast_player_error)
        handler.removeCallbacks(onCompletionTask)
        handler.postDelayed(onCompletionTask, 1000)
        return true
    }

    override fun onInfo(
        what: Int,
        extra: Int
    ): Boolean = false

    override fun onCompletion() {
        if (!error && repeatMode == RepeatMode.REPEAT_ONE) {
            playerModel.seekTo(0)
            return
        }
        onCompletionListener?.invoke()
    }

    fun hasError(): Boolean = error

    companion object {
        private fun makeTimeText(millisecond: Int): String = String.format(
            Locale.US, "%01d:%02d:%02d",
            (millisecond / 3600000).toLong(),
            (millisecond / 60000 % 60).toLong(),
            (millisecond / 1000 % 60).toLong()
        )
    }
}
