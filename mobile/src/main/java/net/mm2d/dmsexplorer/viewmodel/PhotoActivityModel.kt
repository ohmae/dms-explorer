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
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
class PhotoActivityModel(
    private val activity: BaseActivity,
    repository: Repository,
) : BaseObservable() {
    private val targetModel: PlaybackTargetModel = repository.playbackTargetModel
        ?: throw IllegalStateException()

    val title: String

    @ColorInt
    val background: Int

    @get:Bindable
    var imageBinary: ByteArray? = null
        set(imageBinary) {
            field = imageBinary
            notifyPropertyChanged(BR.imageBinary)
        }

    @get:Bindable
    var isLoading = true
        set(loading) {
            field = loading
            notifyPropertyChanged(BR.loading)
        }

    @get:Bindable
    var rightNavigationSize: Int = 0
        set(size) {
            field = size
            notifyPropertyChanged(BR.rightNavigationSize)
        }

    init {
        val settings = Settings.get()
        title = if (settings.shouldShowTitleInPhotoUi()) {
            targetModel.title.toDisplayableString()
        } else {
            ""
        }
        background = if (settings.isPhotoUiBackgroundTransparent) {
            Color.TRANSPARENT
        } else {
            ContextCompat.getColor(activity, R.color.translucent_control)
        }

        val uri = targetModel.uri
        check(!(uri === Uri.EMPTY))
        Downloader.create(uri.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { data ->
                    val model = repository.playbackTargetModel
                    if (model?.uri == uri) {
                        isLoading = false
                        imageBinary = data
                    }
                },
                { Toaster.show(activity, R.string.toast_download_error) },
            )
    }

    fun adjustPanel(activity: Activity) {
        rightNavigationSize = DisplaySizeUtils.getNavigationBarArea(activity).x
    }

    fun onClickBack() {
        activity.navigateUpTo()
    }
}
