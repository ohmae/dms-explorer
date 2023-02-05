/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.databinding.DataBindingUtil
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.MovieActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.FullscreenHelper
import net.mm2d.dmsexplorer.util.RepeatIntroductionUtils
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.MovieActivityModel
import java.util.concurrent.TimeUnit

/**
 * 動画再生のActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MovieActivity : BaseActivity() {
    private lateinit var settings: Settings
    private lateinit var fullscreenHelper: FullscreenHelper
    private lateinit var binding: MovieActivityBinding
    private var model: MovieActivityModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings.get()
        setTheme(settings.themeParams.fullscreenThemeId)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.movie_activity)
        fullscreenHelper = FullscreenHelper(
            rootView = binding.getRoot(),
            topView = binding.toolbar,
            bottomView = binding.controlPanel.root
        )
        val repository = Repository.get()
        try {
            model = MovieActivityModel(this, binding.videoView, repository)
        } catch (ignored: IllegalStateException) {
            finish()
            return
        }
        val model = model!!
        model.setOnChangeContentListener(this::onChangeContent)
        binding.model = model
        model.adjustPanel(this)
        if (RepeatIntroductionUtils.show(this, binding.repeatButton)) {
            val timeout = RepeatIntroductionUtils.TIMEOUT + TIMEOUT_DELAY
            fullscreenHelper.showNavigation(timeout)
        } else {
            if (settings.shouldShowMovieUiOnStart()) {
                fullscreenHelper.showNavigation()
            } else {
                fullscreenHelper.hideNavigationImmediately()
            }
        }
        savedInstanceState?.let {
            model.restoreSaveProgress(it.getInt(KEY_POSITION, 0))
        }
        binding.root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            model.adjustPanel(this)
        }
        addOnPictureInPictureModeChangedListener {
            fullscreenHelper.onPictureInPictureModeChanged(it.isInPictureInPictureMode)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model?.terminate()
        fullscreenHelper.terminate()
    }

    override fun updateOrientationSettings() {
        settings.movieOrientation
            .setRequestedOrientation(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        model?.updateTargetModel()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && event.keyCode != KeyEvent.KEYCODE_BACK) {
            if (fullscreenHelper.showNavigation()) {
                binding.controlPanel.playPause.requestFocus()
            }
            val control = model!!.controlPanelModel
            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> control.onClickPlayPause()
                KeyEvent.KEYCODE_MEDIA_PLAY -> control.onClickPlay()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> control.onClickPause()
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> control.onClickNext()
                KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> control.onClickPrevious()
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val result = super.dispatchTouchEvent(ev)
        if (settings.shouldShowMovieUiOnTouch()) {
            fullscreenHelper.showNavigation()
        }
        return result
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        model?.let { outState.putInt(KEY_POSITION, it.currentProgress) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        model?.adjustPanel(this)
    }

    private fun onChangeContent() {
        if (settings.shouldShowMovieUiOnStart()) {
            fullscreenHelper.showNavigation()
        }
    }

    companion object {
        private const val KEY_POSITION = "KEY_POSITION"
        private val TIMEOUT_DELAY = TimeUnit.SECONDS.toMillis(1)
    }
}
