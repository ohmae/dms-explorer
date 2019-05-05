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
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import net.mm2d.android.util.AribUtils
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.BR
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
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class DmcActivityModel(
    private val activity: Activity,
    repository: Repository
) : BaseObservable(), StatusListener {
    private val handler = Handler(Looper.getMainLooper())
    private val targetModel: PlaybackTargetModel = repository.playbackTargetModel!!
    private val rendererModel: PlayerModel = repository.mediaRendererModel!!
    private var tracking: Boolean = false
    private val trackingCancelTask: Runnable = Runnable { tracking = false }
    val title: String
    val subtitle: String
    val propertyAdapter: PropertyAdapter
    @DrawableRes
    val imageResource: Int
    val isPlayControlEnabled: Boolean
    val hasDuration: Boolean
    val seekBarListener: ScrubBarListener

    @get:Bindable
    var progressText = makeTimeText(0)
        private set
    @get:Bindable
    var durationText = makeTimeText(0)
        private set
    private var mPlaying: Boolean = false
    @get:Bindable
    var isPrepared: Boolean = false
        private set(prepared) {
            field = prepared
            notifyPropertyChanged(BR.prepared)
        }
    @get:Bindable
    var duration: Int = 0
        set(duration) {
            field = duration
            notifyPropertyChanged(BR.duration)
            if (duration > 0) {
                isSeekable = true
            }
            setDurationText(duration)
            isPrepared = true
            setChapterInfoEnabled()
        }
    @get:Bindable
    var progress: Int = 0
        set(progress) {
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
    var scrubText = ""
        private set(scrubText) {
            field = scrubText
            notifyPropertyChanged(BR.scrubText)
        }
    @get:Bindable
    var chapterList = emptyList<Int>()
        private set(chapterList) {
            field = chapterList
            notifyPropertyChanged(BR.chapterList)
            setChapterInfoEnabled()
            if (chapterList.isEmpty()) {
                return
            }
            handler.post {
                val count = propertyAdapter.itemCount
                propertyAdapter.addEntry(
                    activity.getString(R.string.prop_chapter_info),
                    makeChapterString(chapterList)
                )
                propertyAdapter.notifyItemInserted(count)
            }
        }
    @get:Bindable
    var isChapterInfoEnabled: Boolean = false
        private set
    @get:Bindable
    var playButtonResId: Int = R.drawable.ic_play
        private set(resId) {
            field = resId
            notifyPropertyChanged(BR.playButtonResId)
        }

    init {
        if (targetModel.uri === Uri.EMPTY) {
            throw IllegalStateException()
        }
        rendererModel.setStatusListener(this)
        val entity = targetModel.contentEntity
        title = AribUtils.toDisplayableString(entity.name)
        hasDuration = entity.type.hasDuration
        isPlayControlEnabled = hasDuration && rendererModel.canPause()
        val serverModel = repository.mediaServerModel!!
        subtitle = (rendererModel.name
                + "  ←  "
                + serverModel.mediaServer.friendlyName)
        propertyAdapter = PropertyAdapter.ofContent(activity, entity)
        imageResource = getImageResource(entity)
        seekBarListener = object : ScrubBarListener {
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
                handler.removeCallbacks(trackingCancelTask)
                tracking = true
            }

            override fun onStopTrackingTouch(seekBar: ScrubBar) {
                rendererModel.seekTo(seekBar.progress)
                handler.postDelayed(trackingCancelTask, TRACKING_DELAY)
                scrubText = ""
            }

            override fun onAccuracyChanged(
                seekBar: ScrubBar,
                @Accuracy accuracy: Int
            ) {
                scrubText = getAccuracyText(accuracy)
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
        progressText = makeTimeText(progress)
        notifyPropertyChanged(BR.progressText)
    }

    private fun setDurationText(duration: Int) {
        durationText = makeTimeText(duration)
        notifyPropertyChanged(BR.durationText)
    }

    private fun setPlaying(playing: Boolean) {
        if (mPlaying == playing) {
            return
        }
        mPlaying = playing
        playButtonResId = if (playing) R.drawable.ic_pause else R.drawable.ic_play
    }

    private fun getAccuracyText(accuracy: Int): String {
        return when (accuracy) {
            ScrubBar.ACCURACY_NORMAL -> activity.getString(R.string.seek_bar_scrub_normal)
            ScrubBar.ACCURACY_HALF -> activity.getString(R.string.seek_bar_scrub_half)
            ScrubBar.ACCURACY_QUARTER -> activity.getString(R.string.seek_bar_scrub_quarter)
            else -> ""
        }
    }

    private fun setChapterInfoEnabled() {
        isChapterInfoEnabled = duration != 0 && chapterList.isNotEmpty()
        notifyPropertyChanged(BR.chapterInfoEnabled)
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
        this.duration = duration
    }

    override fun notifyProgress(progress: Int) {
        if (!tracking) {
            this.progress = progress
        }
    }

    override fun notifyPlayingState(playing: Boolean) {
        setPlaying(playing)
    }

    override fun notifyChapterList(chapterList: List<Int>) {
        this.chapterList = chapterList
    }

    override fun onError(
        what: Int,
        extra: Int
    ): Boolean {
        Toaster.show(activity, R.string.toast_command_error)
        return false
    }

    override fun onInfo(
        what: Int,
        extra: Int
    ): Boolean {
        return false
    }

    override fun onCompletion() {
        ActivityCompat.finishAfterTransition(activity)
    }

    companion object {
        private const val TRACKING_DELAY = 1000L
        private const val EN_SPACE: Char = 0x2002.toChar() // &ensp;

        @DrawableRes
        private fun getImageResource(entity: ContentEntity): Int {
            return when (entity.type) {
                ContentType.MOVIE -> R.drawable.ic_movie
                ContentType.MUSIC -> R.drawable.ic_music
                ContentType.PHOTO -> R.drawable.ic_image
                else -> 0
            }
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
