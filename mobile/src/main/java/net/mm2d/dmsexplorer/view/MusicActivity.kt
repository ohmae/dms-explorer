/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.os.Bundle
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import kotlinx.coroutines.Job
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ControlPanelBinding
import net.mm2d.dmsexplorer.databinding.MusicActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.RepeatIntroductionUtils
import net.mm2d.dmsexplorer.util.observe
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel
import net.mm2d.dmsexplorer.viewmodel.ControlPanelParam
import net.mm2d.dmsexplorer.viewmodel.MusicActivityModel
import net.mm2d.dmsexplorer.viewmodel.adapter.ImageViewBindingAdapter

/**
 * 音楽再生のActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MusicActivity : BaseActivity() {
    private lateinit var settings: Settings
    private var model: MusicActivityModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings.get()
        setTheme(settings.themeParams.noActionBarThemeId)
        super.onCreate(savedInstanceState)
        val repository = Repository.get()
        val binding: MusicActivityBinding = MusicActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            val model = MusicActivityModel(this, repository)
            this.model = model
            model.getControlColorFlow().observe(this) {
                binding.toolbar.setBackgroundColor(it)
            }
            model.getTitleFlow().observe(this) {
                binding.toolbar.title = it
            }
            model.getRepeatIconIdFlow().observe(this) {
                binding.repeatButton.setImageResource(it)
            }
            binding.repeatButton.setOnClickListener { model.onClickRepeat() }
            model.getImageBinaryFlow().observe(this) {
                ImageViewBindingAdapter.setImageBinary(binding.albumArt, it)
            }
            model.getPropertyAdapterFlow().observe(this) {
                binding.detail.adapter = it
            }
            applyControlPanelParam(binding.controlPanel, model.controlPanelParam)
            model.getControlPanelModelFlow().observe(this) {
                applyControlPanelModel(binding.controlPanel, it)
            }
        } catch (ignored: IllegalStateException) {
            return
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        savedInstanceState?.let {
            model?.restoreSaveProgress(it.getInt(KEY_POSITION, 0))
        }
        RepeatIntroductionUtils.show(this, binding.repeatButton)
    }

    private fun applyControlPanelParam(binding: ControlPanelBinding, param: ControlPanelParam) {
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

    private fun applyControlPanelModel(binding: ControlPanelBinding, model: ControlPanelModel) {
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
    }

    override fun updateOrientationSettings() {
        settings.musicOrientation
            .setRequestedOrientation(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        model?.let { outState.putInt(KEY_POSITION, it.currentProgress) }
    }

    companion object {
        private const val KEY_POSITION = "KEY_POSITION"
    }
}
