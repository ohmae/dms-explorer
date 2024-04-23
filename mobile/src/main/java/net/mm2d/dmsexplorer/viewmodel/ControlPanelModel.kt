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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.domain.model.PlayerModel
import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener
import net.mm2d.dmsexplorer.settings.RepeatMode
import net.mm2d.dmsexplorer.view.view.ScrubBar
import net.mm2d.dmsexplorer.view.view.ScrubBar.Accuracy
import net.mm2d.dmsexplorer.view.view.ScrubBar.ScrubBarListener
import java.util.Locale

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ControlPanelModel internal constructor(
    private val context: Context,
    private val playerModel: PlayerModel,
) : StatusListener {
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

    private val progressTextFlow: MutableStateFlow<String> = MutableStateFlow(makeTimeText(0))
    fun getProgressTextFlow(): Flow<String> = progressTextFlow
    private val durationTextFlow: MutableStateFlow<String> = MutableStateFlow(makeTimeText(0))
    fun getDurationTextFlow(): Flow<String> = durationTextFlow

    var isPlaying: Boolean = false
        private set(playing) {
            if (isPlaying == playing) {
                return
            }
            field = playing
            playButtonResIdFlow.value = if (playing) R.drawable.ic_pause else R.drawable.ic_play
        }

    private val isPrepredFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsPreparedFlow(): Flow<Boolean> = isPrepredFlow

    private val durationFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getDurationFlow(): Flow<Int> = durationFlow
    private fun setDuration(duration: Int) {
        durationFlow.value = duration
        if (duration > 0) {
            isSeekableFlow.value = true
        }
        setDurationText(duration)
        isPrepredFlow.value = true
    }

    private val progressFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getProgressFlow(): Flow<Int> = progressFlow
    fun getProgress(): Int = progressFlow.value
    private fun setProgress(progress: Int) {
        if (tracking) {
            return
        }
        setProgressText(progress)
        progressFlow.value = progress
    }

    private val isSeekableFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsSeekableFlow(): Flow<Boolean> = isSeekableFlow

    private val playButtonResIdFlow: MutableStateFlow<Int> = MutableStateFlow(R.drawable.ic_play)
    fun getPlayButtonResIdFlow(): Flow<Int> = playButtonResIdFlow

    private val scrubTextFlow: MutableStateFlow<String> = MutableStateFlow("")
    fun getScrubTextFlow(): Flow<String> = scrubTextFlow

    private val isNextEnabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsNextEnabledFlow(): Flow<Boolean> = isNextEnabledFlow

    private val isPreviousEnabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsPreviousEnabledFlow(): Flow<Boolean> = isPreviousEnabledFlow

    val seekBarListener: ScrubBarListener = object : ScrubBarListener {
        override fun onProgressChanged(
            seekBar: ScrubBar,
            progress: Int,
            fromUser: Boolean,
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
            scrubTextFlow.value = ""
        }

        override fun onAccuracyChanged(
            seekBar: ScrubBar,
            @Accuracy accuracy: Int,
        ) {
            scrubTextFlow.value = getScrubText(accuracy)
        }
    }

    init {
        playerModel.setStatusListener(this)
        isPreviousEnabledFlow.value = true
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
        isNextEnabledFlow.value = when (mode) {
            RepeatMode.PLAY_ONCE,
            RepeatMode.REPEAT_ONE,
            -> false

            RepeatMode.SEQUENTIAL,
            RepeatMode.REPEAT_ALL,
            -> true
        }
    }

    fun onClickNext() {
        if (!isNextEnabledFlow.value) {
            return
        }
        if (!playerModel.next()) {
            onNextListener?.invoke()
        }
    }

    fun onClickPrevious() {
        if (!isPreviousEnabledFlow.value) {
            return
        }
        if (!playerModel.previous()) {
            onPreviousListener?.invoke()
        }
    }

    private fun setProgressText(progress: Int) {
        progressTextFlow.value = makeTimeText(progress)
    }

    private fun setDurationText(duration: Int) {
        durationTextFlow.value = makeTimeText(duration)
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
        durationFlow.value = duration
    }

    override fun notifyProgress(progress: Int) {
        progressFlow.value = progress
    }

    override fun notifyPlayingState(playing: Boolean) {
        isPlaying = playing
    }

    override fun notifyChapterList(chapterList: List<Int>) {}

    override fun onError(
        what: Int,
        extra: Int,
    ): Boolean {
        error = true
        Toaster.show(context, R.string.toast_player_error)
        handler.removeCallbacks(onCompletionTask)
        handler.postDelayed(onCompletionTask, 1000)
        return true
    }

    override fun onInfo(
        what: Int,
        extra: Int,
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
            Locale.US,
            "%01d:%02d:%02d",
            (millisecond / 3600000).toLong(),
            (millisecond / 60000 % 60).toLong(),
            (millisecond / 1000 % 60).toLong(),
        )
    }
}
