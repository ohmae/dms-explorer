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
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import kotlinx.coroutines.Job
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ControlPanelBinding
import net.mm2d.dmsexplorer.databinding.MovieActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.FullscreenHelper
import net.mm2d.dmsexplorer.util.RepeatIntroductionUtils
import net.mm2d.dmsexplorer.util.observe
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel
import net.mm2d.dmsexplorer.viewmodel.ControlPanelParam
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

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        settings = Settings.get()
        setTheme(settings.themeParams.fullscreenThemeId)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = MovieActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fullscreenHelper = FullscreenHelper(
            window = window,
            rootView = binding.root,
            topView = binding.toolbar,
            bottomView = binding.controlPanel.root,
        )
        val repository = Repository.get()
        try {
            val model = MovieActivityModel(this, binding.videoView, repository)
            this.model = model
            model.setOnChangeContentListener(this::onChangeContent)
            model.adjustPanel(this)
            savedInstanceState?.let {
                model.restoreSaveProgress(it.getInt(KEY_POSITION, 0))
            }
            binding.root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                model.adjustPanel(this)
            }
            binding.toolbar.setBackgroundColor(model.background)
            model.getRightNavigationSizeFlow().observe(this) {
                binding.toolbar.updateLayoutParams<FrameLayout.LayoutParams> {
                    rightMargin = it
                }
            }
            binding.toolbarBack.setOnClickListener { model.onClickBack() }
            model.getTitleFlow().observe(this) {
                binding.toolbarTitle.text = it
            }
            binding.pictureInPictureButton.isVisible = model.canUsePictureInPicture
            binding.pictureInPictureButton.setOnClickListener { model.onClickPictureInPicture() }
            model.getRepeatIconIdFlow().observe(this) {
                binding.repeatButton.setImageResource(it)
            }
            binding.repeatButton.setOnClickListener { model.onClickRepeat() }
            applyControlPanelParam(binding.controlPanel, model.controlPanelParam)
            model.getControlPanelModelFlow().observe(this) {
                applyControlPanelModel(binding.controlPanel, it)
            }
        } catch (ignored: IllegalStateException) {
            finish()
            return
        }
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
        addOnPictureInPictureModeChangedListener {
            fullscreenHelper.onPictureInPictureModeChanged(it.isInPictureInPictureMode)
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(systemBars.left, 0, systemBars.right, 0)
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            binding.controlPanel.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
    }

    private fun applyControlPanelParam(
        binding: ControlPanelBinding,
        param: ControlPanelParam,
    ) {
        param.getMarginRightFlow().observe(this) {
            binding.root.updatePadding(right = it)
        }
        param.getBackgroundColorFlow().observe(this) {
            binding.seekBar.setBottomBackgroundColor(it)
            binding.controlPanel.setBackgroundColor(it)
        }
        param.getBottomPaddingFlow().observe(this) {
            binding.controlPanel.updatePaddingRelative(bottom = it)
        }
    }

    private val jobs: MutableList<Job> = mutableListOf()

    private fun applyControlPanelModel(
        binding: ControlPanelBinding,
        model: ControlPanelModel,
    ) {
        jobs.forEach { it.cancel() }
        jobs.clear()

        jobs += model.getIsSeekableFlow().observe(this) {
            binding.seekBar.isEnabled = it
        }
        jobs += model.getProgressFlow().observe(this) {
            binding.seekBar.progress = it
        }
        jobs += model.getDurationFlow().observe(this) {
            binding.seekBar.max = it
        }
        binding.seekBar.setScrubBarListener(model.seekBarListener)
        jobs += model.getPlayButtonResIdFlow().observe(this) {
            binding.playPause.setImageResource(it)
        }
        jobs += model.getIsPreparedFlow().observe(this) {
            binding.playPause.isEnabled = it
        }
        binding.playPause.setOnClickListener { model.onClickPlayPause() }
        jobs += model.getIsPreviousEnabledFlow().observe(this) {
            binding.previous.isEnabled = it
            binding.previous.alpha = if (it) 1f else 0.5f
        }
        binding.previous.setOnClickListener { model.onClickPrevious() }
        jobs += model.getIsNextEnabledFlow().observe(this) {
            binding.next.isEnabled = it
            binding.next.alpha = if (it) 1f else 0.5f
        }
        binding.next.setOnClickListener { model.onClickNext() }
        jobs += model.getScrubTextFlow().observe(this) {
            binding.scrubText.text = it
        }
        jobs += model.getDurationTextFlow().observe(this) {
            binding.textDuration.text = it
        }
        jobs += model.getProgressTextFlow().observe(this) {
            binding.textProgress.text = it
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

    override fun onNewIntent(
        intent: Intent,
    ) {
        super.onNewIntent(intent)
        model?.updateTargetModel()
    }

    override fun dispatchKeyEvent(
        event: KeyEvent,
    ): Boolean {
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

    override fun dispatchTouchEvent(
        ev: MotionEvent,
    ): Boolean {
        val result = super.dispatchTouchEvent(ev)
        if (settings.shouldShowMovieUiOnTouch()) {
            fullscreenHelper.showNavigation()
        }
        return result
    }

    override fun onSaveInstanceState(
        outState: Bundle,
    ) {
        super.onSaveInstanceState(outState)
        model?.let { outState.putInt(KEY_POSITION, it.currentProgress) }
    }

    override fun onConfigurationChanged(
        newConfig: Configuration,
    ) {
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
