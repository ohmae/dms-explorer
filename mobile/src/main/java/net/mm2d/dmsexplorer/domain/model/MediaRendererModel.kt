/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.upnp.avt.MediaRenderer
import net.mm2d.android.upnp.avt.TransportState
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.android.upnp.cds.chapter.ChapterFetcherFactory
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener
import net.mm2d.log.Logger
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MediaRendererModel(
    context: Context,
    val mediaRenderer: MediaRenderer
) : PlayerModel {
    private var statusListener: StatusListener = STATUS_LISTENER
    private val handler = Handler(Looper.getMainLooper())
    private var chapterList = emptyList<Int>()
    private var started: Boolean = false
    private var stoppingCount: Int = 0
    private val getPositionTask: Runnable
    private val wifiLock: WifiManager.WifiLock

    override var isPlaying: Boolean = false
    override var progress: Int = 0
    override var duration: Int = 0
    override val name: String
        get() = mediaRenderer.friendlyName

    private val currentChapter: Int
        get() {
            if (chapterList.isEmpty()) {
                return 0
            }
            val progress = this.progress
            for (i in chapterList.indices) {
                if (progress < chapterList[i]) {
                    return i - 1
                }
            }
            return chapterList.size - 1
        }

    init {
        val wm = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG)
        wifiLock.setReferenceCounted(true)
        wifiLock.acquire()
        getPositionTask = Runnable {
            mediaRenderer.getPositionInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { onGetPositionInfo(it) })
            mediaRenderer.getTransportInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { onGetTransportInfo(it) })
        }
    }

    override fun canPause(): Boolean {
        return mediaRenderer.isSupportPause()
    }

    override fun terminate() {
        if (!started) {
            return
        }
        if (wifiLock.isHeld) {
            wifiLock.release()
        }
        statusListener = STATUS_LISTENER
        handler.removeCallbacks(getPositionTask)
        mediaRenderer.stop()
            .subscribeOn(Schedulers.io())
            .subscribe()
        mediaRenderer.clearAVTransportURI()
            .subscribeOn(Schedulers.io())
            .subscribe()
        mediaRenderer.unsubscribe()
        started = false
    }

    override fun setStatusListener(listener: StatusListener) {
        statusListener = listener
    }

    @SuppressLint("CheckResult")
    override fun setUri(
        uri: Uri,
        entity: ContentEntity?
    ) {
        mediaRenderer.clearAVTransportURI()
            .subscribeOn(Schedulers.io())
            .subscribe()
        val cdsObject = entity?.rawEntity as CdsObject
        mediaRenderer.setAVTransportURI(cdsObject, uri.toString())
            .subscribeOn(Schedulers.io())
            .flatMap { mediaRenderer.play() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }, { onError() })
        stoppingCount = 0
        handler.postDelayed(getPositionTask, 1000)
        ChapterFetcherFactory.create(cdsObject)
            .subscribe({ setChapterList(it) }, { Logger.w(it) })
        started = true
    }

    override fun restoreSaveProgress(progress: Int) {
        this.progress = progress
    }

    @SuppressLint("CheckResult")
    override fun play() {
        mediaRenderer.play()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }, { onError() })
    }

    @SuppressLint("CheckResult")
    override fun pause() {
        mediaRenderer.pause()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }, { onError() })
    }

    @SuppressLint("CheckResult")
    override fun seekTo(position: Int) {
        mediaRenderer.seek(position.toLong())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }, { onError() })
        stoppingCount = 0
        handler.removeCallbacks(getPositionTask)
        handler.postDelayed(getPositionTask, 1000)
    }

    @SuppressLint("CheckResult")
    override operator fun next(): Boolean {
        if (chapterList.isEmpty()) {
            return false
        }
        val chapter = currentChapter + 1
        if (chapter < chapterList.size) {
            mediaRenderer.seek(chapterList[chapter].toLong())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }, { onError() })
            return true
        }
        return false
    }

    @SuppressLint("CheckResult")
    override fun previous(): Boolean {
        if (chapterList.isEmpty()) {
            return false
        }
        var chapter = currentChapter
        if (chapter > 0 && progress - chapterList[chapter] < CHAPTER_MARGIN) {
            chapter--
        }
        if (chapter >= 0) {
            mediaRenderer.seek(chapterList[chapter].toLong())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }, { onError() })
            return true
        }
        return false
    }

    private fun onGetPositionInfo(result: Map<String, String>?) {
        if (result == null) {
            handler.postDelayed(getPositionTask, 1000)
            return
        }
        val duration = MediaRenderer.getDuration(result)
        val progress = MediaRenderer.getProgress(result)
        if (duration < 0 || progress < 0) {
            handler.postDelayed(getPositionTask, 1000)
            return
        }
        if (this.duration != duration) {
            this.duration = duration
            statusListener.notifyDuration(duration)
        }
        if (this.progress != progress) {
            this.progress = progress
            statusListener.notifyProgress(progress)
        }
        val interval = (1000 - progress % 1000).toLong()
        handler.postDelayed(getPositionTask, interval)
    }

    private fun onGetTransportInfo(result: Map<String, String>?) {
        if (result == null) {
            return
        }
        val state = MediaRenderer.getCurrentTransportState(result)
        val playing = state === TransportState.PLAYING
        if (isPlaying != playing) {
            isPlaying = playing
            statusListener.notifyPlayingState(playing)
        }
        stoppingCount = if (state === TransportState.STOPPED) stoppingCount + 1 else 0
        if (stoppingCount > STOPPING_THRESHOLD) {
            handler.removeCallbacks(getPositionTask)
            statusListener.onCompletion()
        }
    }

    private fun setChapterList(chapterList: List<Int>) {
        this.chapterList = chapterList
        statusListener.notifyChapterList(chapterList)
    }

    private fun onError() {
        statusListener.onError(0, 0)
    }

    companion object {
        private val TAG: String = MediaRendererModel::class.java.simpleName
        private val CHAPTER_MARGIN: Int = TimeUnit.SECONDS.toMillis(5).toInt()
        private const val STOPPING_THRESHOLD: Int = 5
        private val STATUS_LISTENER: StatusListener = StatusListenerAdapter()
    }
}
