/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.util.AribUtils
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.BR
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.model.MediaServerModel
import net.mm2d.dmsexplorer.domain.model.MusicPlayerModel
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.RepeatMode
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.Downloader
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.OnCompletionListener
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.SkipControlListener
import net.mm2d.dmsexplorer.viewmodel.helper.MuteAlertHelper

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MusicActivityModel(
    private val mActivity: BaseActivity,
    private val mRepository: Repository
) : BaseObservable(), OnCompletionListener, SkipControlListener {
    private val serverModel: MediaServerModel = mRepository.mediaServerModel
    private val settings: Settings = Settings.get()
    private var repeatMode: RepeatMode = settings.repeatModeMusic
    private var toast: Toast? = null
    private val muteAlertHelper: MuteAlertHelper = MuteAlertHelper(mActivity)
    private var playStartTime: Long = 0
    private var finishing: Boolean = false
    val controlPanelParam: ControlPanelParam = ControlPanelParam()
    @get:Bindable
    var title = ""
        private set
    @get:Bindable
    var controlColor: Int = 0
        private set
    @get:Bindable
    lateinit var controlPanelModel: ControlPanelModel
        private set
    @get:Bindable
    lateinit var propertyAdapter: PropertyAdapter
        private set
    @get:Bindable
    var imageBinary: ByteArray? = null
        set(imageBinary) {
            field = imageBinary
            notifyPropertyChanged(BR.imageBinary)
        }

    val currentProgress: Int
        get() = controlPanelModel.progress

    @DrawableRes
    @get:Bindable
    var repeatIconId: Int = repeatMode.iconId
        set(@DrawableRes id) {
            field = id
            notifyPropertyChanged(BR.repeatIconId)
        }

    private val isTooShortPlayTime: Boolean
        get() {
            val playTime = System.currentTimeMillis() - playStartTime
            return !controlPanelModel.isSkipped && playTime < TOO_SHORT_PLAY_TIME
        }

    init {
        val targetModel = mRepository.playbackTargetModel
        if (targetModel == null || targetModel.uri === Uri.EMPTY) {
            throw IllegalStateException()
        }
        updateTargetModel()
    }

    private fun updateTargetModel() {
        val targetModel = mRepository.playbackTargetModel
        if (targetModel == null || targetModel.uri === Uri.EMPTY) {
            finishAfterTransition()
            return
        }
        playStartTime = System.currentTimeMillis()
        muteAlertHelper.alertIfMuted()
        val playerModel = MusicPlayerModel(mActivity)
        controlPanelModel = ControlPanelModel(mActivity, playerModel).also {
            it.setRepeatMode(repeatMode)
            it.setOnCompletionListener(this)
            it.setSkipControlListener(this)
        }
        playerModel.setUri(targetModel.uri, null)

        title = AribUtils.toDisplayableString(targetModel.title)
        val generator = settings
            .themeParams
            .themeColorGenerator
        controlColor = generator.getControlColor(title)
        propertyAdapter = PropertyAdapter.ofContent(mActivity, targetModel.contentEntity)
        mRepository.themeModel.setThemeColor(mActivity, controlColor, 0)

        notifyPropertyChanged(BR.title)
        notifyPropertyChanged(BR.controlColor)
        notifyPropertyChanged(BR.propertyAdapter)
        notifyPropertyChanged(BR.controlPanelModel)

        controlPanelParam.backgroundColor = controlColor

        loadArt(targetModel.contentEntity.artUri)
    }

    private fun loadArt(uri: Uri) {
        imageBinary = null
        if (uri === Uri.EMPTY) {
            return
        }
        Downloader.create(uri.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer<ByteArray> { imageBinary = it }
            ).isDisposed
    }

    fun terminate() {
        controlPanelModel.terminate()
    }

    fun restoreSaveProgress(position: Int) {
        controlPanelModel.restoreSaveProgress(position)
    }

    fun onClickRepeat() {
        repeatMode = repeatMode.next()
        controlPanelModel.setRepeatMode(repeatMode)
        repeatIconId = repeatMode.iconId
        settings.repeatModeMusic = repeatMode

        showRepeatToast()
    }

    private fun showRepeatToast() {
        if (toast != null) {
            toast!!.cancel()
        }
        toast = Toaster.show(mActivity, repeatMode.messageId)
    }

    override fun onCompletion() {
        controlPanelModel.terminate()
        if (isTooShortPlayTime || controlPanelModel.hasError() || !selectNext()) {
            finishAfterTransition()
            return
        }
        updateTargetModel()
        EventLogger.sendPlayContent(true)
    }

    override fun next() {
        controlPanelModel.terminate()
        if (!selectNext()) {
            finishAfterTransition()
            return
        }
        updateTargetModel()
        EventLogger.sendPlayContent(true)
    }

    override fun previous() {
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
            ActivityCompat.finishAfterTransition(mActivity)
        }
    }

    private fun selectNext(): Boolean {
        return when (repeatMode) {
            RepeatMode.PLAY_ONCE -> false
            RepeatMode.SEQUENTIAL -> serverModel.selectNextEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL)
            RepeatMode.REPEAT_ALL -> serverModel.selectNextEntity(MediaServerModel.SCAN_MODE_LOOP)
            RepeatMode.REPEAT_ONE -> false
        }
    }

    private fun selectPrevious(): Boolean {
        return when (repeatMode) {
            RepeatMode.PLAY_ONCE -> false
            RepeatMode.SEQUENTIAL -> serverModel.selectPreviousEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL)
            RepeatMode.REPEAT_ALL -> serverModel.selectPreviousEntity(MediaServerModel.SCAN_MODE_LOOP)
            RepeatMode.REPEAT_ONE -> false
        }
    }

    companion object {
        private const val TOO_SHORT_PLAY_TIME = 2000L
    }
}
