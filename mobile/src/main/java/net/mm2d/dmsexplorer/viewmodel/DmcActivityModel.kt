/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.mm2d.android.util.AribUtils
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.entity.ContentType
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel
import net.mm2d.dmsexplorer.domain.model.PlayerModel
import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter
import net.mm2d.dmsexplorer.view.view.ScrubBar
import net.mm2d.dmsexplorer.view.view.ScrubBar.Accuracy
import net.mm2d.dmsexplorer.view.view.ScrubBar.ScrubBarListener
import java.util.Locale

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class DmcActivityModel(
    private val activity: Activity,
    repository: Repository,
) : StatusListener {
    private val handler = Handler(Looper.getMainLooper())
    private val targetModel: PlaybackTargetModel = repository.playbackTargetModel
        ?: throw IllegalStateException()
    private val rendererModel: PlayerModel = repository.mediaRendererModel
        ?: throw IllegalStateException()
    private var tracking: Boolean = false
    private val trackingCancelTask: Runnable = Runnable { tracking = false }
    val title: String
    val subtitle: String
    val propertyAdapter: PropertyAdapter
    val imageResource: Int
    val isPlayControlEnabled: Boolean
    val hasDuration: Boolean
    val seekBarListener: ScrubBarListener

    private val progressTextFlow: MutableStateFlow<String> = MutableStateFlow(makeTimeText(0))
    fun getProgressTextFlow(): Flow<String> = progressTextFlow

    private val durationTextFlow: MutableStateFlow<String> = MutableStateFlow(makeTimeText(0))
    fun getDurationTextFlow(): Flow<String> = durationTextFlow

    private var mPlaying: Boolean = false

    private val isPreparedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsPreparedFlow(): Flow<Boolean> = isPreparedFlow

    private val durationFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getDurationFlow(): Flow<Int> = durationFlow
    private fun setDuration(duration: Int) {
        durationFlow.value = duration
        if (duration > 0) {
            isSeekableFlow.value = true
        }
        setDurationText(duration)
        isPreparedFlow.value = true
        setChapterInfoEnabled()
    }

    private val progressFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getProgressFlow(): Flow<Int> = progressFlow
    private fun setProgress(progress: Int) {
        progressFlow.value = progress
        setProgressText(progress)
    }

    private val isSeekableFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsSeekableFlow(): Flow<Boolean> = isSeekableFlow

    private val scrubTextFlow: MutableStateFlow<String> = MutableStateFlow("")
    fun getScrubTextFlow(): Flow<String> = scrubTextFlow

    private val chapterListFlow: MutableStateFlow<List<Int>> = MutableStateFlow(emptyList())
    fun getChapterListFlow(): Flow<List<Int>> = chapterListFlow
    private fun setChapterList(chapterList: List<Int>) {
        chapterListFlow.value = chapterList
        setChapterInfoEnabled()
        if (chapterList.isEmpty()) {
            return
        }
        handler.post {
            val count = propertyAdapter.itemCount
            propertyAdapter.addEntry(
                activity.getString(R.string.prop_chapter_info),
                makeChapterString(chapterList),
            )
            propertyAdapter.notifyItemInserted(count)
        }
    }

    private val isChapterInfoEnabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsChapterInfoEnabledFlow(): Flow<Boolean> = isChapterInfoEnabledFlow

    private val playButtonResIdFlow: MutableStateFlow<Int> = MutableStateFlow(R.drawable.ic_play)
    fun getPlayButtonResIdFlow(): Flow<Int> = playButtonResIdFlow

    init {
        check(!(targetModel.uri === Uri.EMPTY))
        rendererModel.setStatusListener(this)
        val entity = targetModel.contentEntity
        title = AribUtils.toDisplayableString(entity.name)
        hasDuration = entity.type.hasDuration
        isPlayControlEnabled = hasDuration && rendererModel.canPause()
        val serverModel = repository.mediaServerModel
            ?: throw IllegalStateException()
        subtitle = (
            rendererModel.name +
                "  ←  " +
                serverModel.mediaServer.friendlyName
            )
        propertyAdapter = PropertyAdapter.ofContent(activity, entity)
        imageResource = getImageResource(entity)
        seekBarListener = object : ScrubBarListener {
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
                handler.removeCallbacks(trackingCancelTask)
                tracking = true
            }

            override fun onStopTrackingTouch(seekBar: ScrubBar) {
                rendererModel.seekTo(seekBar.progress)
                handler.postDelayed(trackingCancelTask, TRACKING_DELAY)
                scrubTextFlow.value = ""
            }

            override fun onAccuracyChanged(
                seekBar: ScrubBar,
                @Accuracy accuracy: Int,
            ) {
                scrubTextFlow.value = getAccuracyText(accuracy)
            }
        }
    }

    fun initialize() {
        val uri = targetModel.uri
        rendererModel.setUri(uri, targetModel.contentEntity)
    }

    fun terminate() {
        rendererModel.terminate()
    }

    private fun setProgressText(progress: Int) {
        progressTextFlow.value = makeTimeText(progress)
    }

    private fun setDurationText(duration: Int) {
        durationTextFlow.value = makeTimeText(duration)
    }

    private fun setPlaying(playing: Boolean) {
        if (mPlaying == playing) {
            return
        }
        mPlaying = playing
        playButtonResIdFlow.value = if (playing) R.drawable.ic_pause else R.drawable.ic_play
    }

    private fun getAccuracyText(accuracy: Int): String =
        when (accuracy) {
            ScrubBar.ACCURACY_NORMAL -> activity.getString(R.string.seek_bar_scrub_normal)
            ScrubBar.ACCURACY_HALF -> activity.getString(R.string.seek_bar_scrub_half)
            ScrubBar.ACCURACY_QUARTER -> activity.getString(R.string.seek_bar_scrub_quarter)
            else -> ""
        }

    private fun setChapterInfoEnabled() {
        isChapterInfoEnabledFlow.value = durationFlow.value != 0 && chapterListFlow.value.isNotEmpty()
    }

    fun onClickPlay() {
        if (mPlaying) {
            rendererModel.pause()
        } else {
            rendererModel.play()
        }
    }

    fun onClickNext() {
        rendererModel.next()
    }

    fun onClickPrevious() {
        rendererModel.previous()
    }

    override fun notifyDuration(duration: Int) {
        setDuration(duration)
    }

    override fun notifyProgress(progress: Int) {
        if (!tracking) {
            setProgress(progress)
        }
    }

    override fun notifyPlayingState(playing: Boolean) {
        setPlaying(playing)
    }

    override fun notifyChapterList(chapterList: List<Int>) {
        setChapterList(chapterList)
    }

    override fun onError(
        what: Int,
        extra: Int,
    ): Boolean {
        Toaster.show(activity, R.string.toast_command_error)
        return false
    }

    override fun onInfo(
        what: Int,
        extra: Int,
    ): Boolean = false

    override fun onCompletion() {
        ActivityCompat.finishAfterTransition(activity)
    }

    companion object {
        private const val TRACKING_DELAY = 1000L
        private const val EN_SPACE: Char = 0x2002.toChar() // &ensp;

        @DrawableRes
        private fun getImageResource(entity: ContentEntity): Int =
            when (entity.type) {
                ContentType.MOVIE -> R.drawable.ic_movie
                ContentType.MUSIC -> R.drawable.ic_music
                ContentType.PHOTO -> R.drawable.ic_image
                else -> 0
            }

        private fun makeTimeText(millisecond: Int): String {
            val second = (millisecond / 1000 % 60).toLong()
            val minute = (millisecond / 60000 % 60).toLong()
            val hour = (millisecond / 3600000).toLong()
            return String.format(Locale.US, "%01d:%02d:%02d", hour, minute, second)
        }

        private fun makeChapterString(chapterList: List<Int>): String {
            val sb = StringBuilder()
            for (i in chapterList.indices) {
                if (sb.isNotEmpty()) {
                    sb.append("\n")
                }
                if (i < 9) {
                    sb.append(EN_SPACE)
                }
                sb.append((i + 1).toString())
                sb.append(" : ")
                val chapter = chapterList[i]
                sb.append(makeTimeText(chapter))
            }
            return sb.toString()
        }
    }
}
