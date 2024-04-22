/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.mm2d.android.util.DisplaySizeUtils
import net.mm2d.android.util.Toaster
import net.mm2d.android.util.toDisplayableString
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.Downloader
import net.mm2d.dmsexplorer.view.base.BaseActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@SuppressLint("CheckResult")
class PhotoActivityModel(
    private val activity: BaseActivity,
    private val repository: Repository,
) : BaseObservable() {
    private val titleFlow: MutableStateFlow<String> = MutableStateFlow("")
    fun getTitleFlow(): Flow<String> = titleFlow

    @ColorInt
    val background: Int

    private val imageBinaryFlow: MutableStateFlow<ByteArray?> = MutableStateFlow(null)
    fun getImageBinaryFlow(): Flow<ByteArray?> = imageBinaryFlow

    private val isLoadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    fun getIsLoadingFlow(): Flow<Boolean> = isLoadingFlow

    private val rightNavigationSizeFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getRightNavigationSizeFlow(): Flow<Int> = rightNavigationSizeFlow

    init {
        val settings = Settings.get()
        background = if (settings.isPhotoUiBackgroundTransparent) {
            Color.TRANSPARENT
        } else {
            ContextCompat.getColor(activity, R.color.translucent_control)
        }
    }

    fun update() {
        val settings = Settings.get()
        val targetModel: PlaybackTargetModel = repository.playbackTargetModel ?: return
        titleFlow.tryEmit(
            if (settings.shouldShowTitleInPhotoUi()) {
                targetModel.title.toDisplayableString()
            } else {
                ""
            },
        )
        val uri = targetModel.uri
        check(uri !== Uri.EMPTY)
        imageBinaryFlow.tryEmit(null)
        Downloader.create(uri.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { data ->
                    val model = repository.playbackTargetModel
                    if (model?.uri == uri) {
                        imageBinaryFlow.tryEmit(data)
                        isLoadingFlow.tryEmit(false)
                    }
                },
                { Toaster.show(activity, R.string.toast_download_error) },
            )
    }

    fun adjustPanel(activity: Activity) {
        rightNavigationSizeFlow.tryEmit(DisplaySizeUtils.getNavigationBarArea(activity).x)
    }

    fun onClickBack() {
        activity.navigateUpTo()
    }
}
