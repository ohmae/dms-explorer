/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import net.mm2d.android.util.DisplaySizeUtils
import net.mm2d.android.util.Toaster
import net.mm2d.android.util.toDisplayableString
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.model.MediaServerModel
import net.mm2d.dmsexplorer.domain.model.MoviePlayerModel
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.RepeatMode
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.helper.MovieActivityPipHelper
import net.mm2d.dmsexplorer.viewmodel.helper.MuteAlertHelper
import net.mm2d.dmsexplorer.viewmodel.helper.PipHelpers

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MovieActivityModel(
    private val activity: BaseActivity,
    private val videoView: VideoView,
    private val repository: Repository,
) {
    private val serverModel: MediaServerModel? = repository.mediaServerModel
    private val settings: Settings = Settings.get()
    private var onChangeContentListener: (() -> Unit)? = null
    private var repeatMode: RepeatMode = settings.repeatModeMovie
    private var toast: Toast? = null
    private val movieActivityPipHelper: MovieActivityPipHelper
    private val muteAlertHelper: MuteAlertHelper
    private var playStartTime: Long = 0
    private var finishing: Boolean = false

    val controlPanelParam: ControlPanelParam
    val canUsePictureInPicture = PipHelpers.isSupported(activity)

    @ColorInt
    val background: Int
    val currentProgress: Int
        get() = controlPanelModel.getProgress()

    private val titleFlow: MutableStateFlow<String> = MutableStateFlow("")
    fun getTitleFlow(): Flow<String> = titleFlow

    private val controlPanelModelFlow: MutableSharedFlow<ControlPanelModel> =
        MutableSharedFlow(replay = 1, onBufferOverflow = DROP_OLDEST)
    var controlPanelModel: ControlPanelModel
        get() = controlPanelModelFlow.replayCache.first()
        private set(value) {
            controlPanelModelFlow.tryEmit(value)
        }

    fun getControlPanelModelFlow(): Flow<ControlPanelModel> = controlPanelModelFlow

    private val rightNavigationSizeFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getRightNavigationSizeFlow(): Flow<Int> = rightNavigationSizeFlow
    private fun setRightNavigationSize(size: Int) {
        rightNavigationSizeFlow.value = size
        controlPanelParam.setMarginRight(size)
    }

    private val repeatIconIdFlow: MutableStateFlow<Int> = MutableStateFlow(repeatMode.iconId)
    fun getRepeatIconIdFlow(): Flow<Int> = repeatIconIdFlow

    private val isTooShortPlayTime: Boolean
        get() {
            val playTime = System.currentTimeMillis() - playStartTime
            return !controlPanelModel.isSkipped && playTime < TOO_SHORT_PLAY_TIME
        }

    init {
        background = if (settings.isMovieUiBackgroundTransparent) {
            Color.TRANSPARENT
        } else {
            ContextCompat.getColor(activity, R.color.translucent_control)
        }
        controlPanelParam = ControlPanelParam()
        controlPanelParam.setBackgroundColor(background)

        movieActivityPipHelper = PipHelpers.getMovieHelper(activity)
        movieActivityPipHelper.register()
        muteAlertHelper = MuteAlertHelper(activity)
        val targetModel = repository.playbackTargetModel
        check(!(targetModel == null || targetModel.uri === Uri.EMPTY))
        updateTargetModel()
    }

    fun updateTargetModel() {
        val targetModel = repository.playbackTargetModel
        if (targetModel == null || targetModel.uri === Uri.EMPTY) {
            finishAfterTransition()
            return
        }
        playStartTime = System.currentTimeMillis()
        muteAlertHelper.alertIfMuted()
        val playerModel = MoviePlayerModel(activity, videoView)
        val controlPanelModel = ControlPanelModel(activity, playerModel)
        controlPanelModel.setRepeatMode(repeatMode)
        controlPanelModel.setOnCompletionListener(this::onCompletion)
        controlPanelModel.setSkipControlListener(this::onNext, this::onPrevious)
        this.controlPanelModel = controlPanelModel
        movieActivityPipHelper.setControlPanelModel(controlPanelModel)
        playerModel.setUri(targetModel.uri, null)
        titleFlow.value = if (settings.shouldShowTitleInMovieUi()) {
            targetModel.title.toDisplayableString()
        } else {
            ""
        }
    }

    fun adjustPanel(activity: Activity) {
        val size = DisplaySizeUtils.getNavigationBarArea(activity)
        setRightNavigationSize(size.x)
        controlPanelParam.setBottomPadding(size.y)
    }

    fun terminate() {
        controlPanelModel.terminate()
        movieActivityPipHelper.unregister()
    }

    fun restoreSaveProgress(position: Int) {
        controlPanelModel.restoreSaveProgress(position)
    }

    fun setOnChangeContentListener(listener: (() -> Unit)?) {
        onChangeContentListener = listener
    }

    fun onClickBack() {
        activity.navigateUpTo()
    }

    fun onClickRepeat() {
        repeatMode = repeatMode.next()
        controlPanelModel.setRepeatMode(repeatMode)
        repeatIconIdFlow.value = repeatMode.iconId
        settings.repeatModeMovie = repeatMode
        showRepeatToast()
    }

    fun onClickPictureInPicture() {
        movieActivityPipHelper.enterPictureInPictureMode(videoView)
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
        onChangeContentListener?.invoke()
        EventLogger.sendPlayContent(true)
    }

    private fun onNext() {
        controlPanelModel.terminate()
        if (!selectNext()) {
            finishAfterTransition()
            return
        }
        updateTargetModel()
        onChangeContentListener?.invoke()
        EventLogger.sendPlayContent(true)
    }

    private fun onPrevious() {
        controlPanelModel.terminate()
        if (!selectPrevious()) {
            finishAfterTransition()
            return
        }
        updateTargetModel()
        onChangeContentListener?.invoke()
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
