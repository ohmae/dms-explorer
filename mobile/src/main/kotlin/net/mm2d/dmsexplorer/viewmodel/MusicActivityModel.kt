/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import net.mm2d.android.util.AribUtils
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.model.MediaServerModel
import net.mm2d.dmsexplorer.domain.model.MusicPlayerModel
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.RepeatMode
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.Downloader
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.helper.MuteAlertHelper

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MusicActivityModel(
    private val activity: BaseActivity,
    private val repository: Repository,
) {
    private val serverModel: MediaServerModel? = repository.mediaServerModel
    private val settings: Settings = Settings.get()
    private var repeatMode: RepeatMode = settings.repeatModeMusic
    private var toast: Toast? = null
    private val muteAlertHelper: MuteAlertHelper = MuteAlertHelper(activity)
    private var playStartTime: Long = 0
    private var finishing: Boolean = false
    val controlPanelParam: ControlPanelParam = ControlPanelParam()

    private val titleFlow: MutableStateFlow<String> = MutableStateFlow("")
    fun getTitleFlow(): Flow<String> = titleFlow

    private val controlColorFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getControlColorFlow(): Flow<Int> = controlColorFlow

    private val controlPanelModelFlow: MutableStateFlow<ControlPanelModel> =
        MutableStateFlow(ControlPanelModel(activity, MusicPlayerModel(activity)))

    fun getControlPanelModelFlow(): Flow<ControlPanelModel> = controlPanelModelFlow
    private var controlPanelModel: ControlPanelModel
        get() = controlPanelModelFlow.value
        private set(value) {
            controlPanelModelFlow.value = value
        }

    private val propertyAdapterFlow: MutableSharedFlow<PropertyAdapter> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun getPropertyAdapterFlow(): Flow<PropertyAdapter> = propertyAdapterFlow
    private var propertyAdapter: PropertyAdapter
        get() = propertyAdapterFlow.replayCache.first()
        set(value) {
            propertyAdapterFlow.tryEmit(value)
        }

    private val imageBinaryFlow: MutableStateFlow<ByteArray?> = MutableStateFlow(null)
    fun getImageBinaryFlow(): Flow<ByteArray?> = imageBinaryFlow

    val currentProgress: Int
        get() = controlPanelModel.getProgress()

    private val repeatIconIdFlow: MutableStateFlow<Int> = MutableStateFlow(repeatMode.iconId)
    fun getRepeatIconIdFlow(): Flow<Int> = repeatIconIdFlow

    private val isTooShortPlayTime: Boolean
        get() {
            val playTime = System.currentTimeMillis() - playStartTime
            return !controlPanelModel.isSkipped && playTime < TOO_SHORT_PLAY_TIME
        }

    init {
        val targetModel = repository.playbackTargetModel
        check(!(targetModel == null || targetModel.uri === Uri.EMPTY))
        updateTargetModel()
    }

    private fun updateTargetModel() {
        val targetModel = repository.playbackTargetModel
        if (targetModel == null || targetModel.uri === Uri.EMPTY) {
            finishAfterTransition()
            return
        }
        playStartTime = System.currentTimeMillis()
        muteAlertHelper.alertIfMuted()
        val playerModel = MusicPlayerModel(activity)
        controlPanelModel = ControlPanelModel(activity, playerModel).also {
            it.setRepeatMode(repeatMode)
            it.setOnCompletionListener(this::onCompletion)
            it.setSkipControlListener(this::onNext, this::onPrevious)
        }
        playerModel.setUri(targetModel.uri, null)

        titleFlow.value = AribUtils.toDisplayableString(targetModel.title)
        val generator = settings
            .themeParams
            .themeColorGenerator
        controlColorFlow.value = generator.getControlColor(titleFlow.value)
        propertyAdapter = PropertyAdapter.ofContent(activity, targetModel.contentEntity)
        repository.themeModel.setThemeColor(activity, controlColorFlow.value, 0)

        controlPanelParam.setBackgroundColor(controlColorFlow.value)

        loadArt(targetModel.contentEntity.artUri)
    }

    private fun loadArt(
        uri: Uri,
    ) {
        imageBinaryFlow.value = null
        if (uri === Uri.EMPTY) {
            return
        }
        Downloader.create(uri.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                Consumer { imageBinaryFlow.value = it },
            ).isDisposed
    }

    fun terminate() {
        controlPanelModel.terminate()
    }

    fun restoreSaveProgress(
        position: Int,
    ) {
        controlPanelModel.restoreSaveProgress(position)
    }

    fun onClickRepeat() {
        repeatMode = repeatMode.next()
        controlPanelModel.setRepeatMode(repeatMode)
        repeatIconIdFlow.value = repeatMode.iconId
        settings.repeatModeMusic = repeatMode

        showRepeatToast()
    }

    private fun showRepeatToast() {
        toast?.cancel()
        toast = Toaster.show(activity, repeatMode.messageId)
    }

    private fun onCompletion() {
        controlPanelModel.terminate()
        if (isTooShortPlayTime || controlPanelModel.hasError() || !selectNext()) {
            finishAfterTransition()
            return
        }
        updateTargetModel()
        EventLogger.sendPlayContent(true)
    }

    private fun onNext() {
        controlPanelModel.terminate()
        if (!selectNext()) {
            finishAfterTransition()
            return
        }
        updateTargetModel()
        EventLogger.sendPlayContent(true)
    }

    private fun onPrevious() {
        controlPanelModel.terminate()
        if (!selectPrevious()) {
            finishAfterTransition()
            return
        }
        updateTargetModel()
        EventLogger.sendPlayContent(true)
    }

    private fun finishAfterTransition() {
        if (!finishing) {
            finishing = true
            ActivityCompat.finishAfterTransition(activity)
        }
    }

    private fun selectNext(): Boolean =
        when (repeatMode) {
            RepeatMode.PLAY_ONCE -> false
            RepeatMode.SEQUENTIAL ->
                serverModel?.selectNextEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL) ?: false

            RepeatMode.REPEAT_ALL ->
                serverModel?.selectNextEntity(MediaServerModel.SCAN_MODE_LOOP) ?: false

            RepeatMode.REPEAT_ONE -> false
        }

    private fun selectPrevious(): Boolean =
        when (repeatMode) {
            RepeatMode.PLAY_ONCE -> false
            RepeatMode.SEQUENTIAL ->
                serverModel?.selectPreviousEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL) ?: false

            RepeatMode.REPEAT_ALL ->
                serverModel?.selectPreviousEntity(MediaServerModel.SCAN_MODE_LOOP) ?: false

            RepeatMode.REPEAT_ONE -> false
        }

    companion object {
        private const val TOO_SHORT_PLAY_TIME = 2000L
    }
}
