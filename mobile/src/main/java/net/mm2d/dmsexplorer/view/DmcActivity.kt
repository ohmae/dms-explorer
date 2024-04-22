/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.DmcActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.DmcActivityModel

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class DmcActivity : BaseActivity() {
    private var model: DmcActivityModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DmcActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val model = DmcActivityModel(this, Repository.get())
            model.initialize()
            this.model = model
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.title = model.title
            binding.toolbar.subtitle = model.subtitle
            binding.image.setImageResource(model.imageResource)
            binding.detail.adapter = model.propertyAdapter
            binding.controlPanel.isVisible = model.hasDuration
            model.getChapterListFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.seekBar.chapterList = it }
                .launchIn(lifecycleScope)
            model.getIsSeekableFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.seekBar.isEnabled = it }
                .launchIn(lifecycleScope)
            model.getDurationFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.seekBar.max = it }
                .launchIn(lifecycleScope)
            model.getProgressFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.seekBar.progress = it }
                .launchIn(lifecycleScope)
            binding.seekBar.setScrubBarListener(model.seekBarListener)
            model.getProgressTextFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.textProgress.text = it }
                .launchIn(lifecycleScope)
            model.getIsChapterInfoEnabledFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach {
                    binding.previous.isInvisible = !it
                    binding.next.isInvisible = !it
                }
                .launchIn(lifecycleScope)
            binding.previous.setOnClickListener { model.onClickPrevious() }
            binding.next.setOnClickListener { model.onClickNext() }
            model.getPlayButtonResIdFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.play.setImageResource(it) }
                .launchIn(lifecycleScope)
            binding.play.isInvisible = !model.isPlayControlEnabled
            binding.play.setOnClickListener { model.onClickPlay() }
            model.getIsPreparedFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.play.isEnabled = it }
                .launchIn(lifecycleScope)
            model.getDurationTextFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.textDuration.text = it }
                .launchIn(lifecycleScope)
            model.getScrubTextFlow()
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .onEach { binding.textScrub.text = it }
                .launchIn(lifecycleScope)
        } catch (ignored: IllegalStateException) {
            finish()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model?.terminate()
        Repository.get().controlPointModel.clearSelectedRenderer()
    }

    override fun updateOrientationSettings() {
        Settings.get().dmcOrientation
            .setRequestedOrientation(this)
    }

    companion object {
        /**
         * このActivityを起動するためのIntentを作成する。
         *
         * Extraの設定と読み出しをこのクラス内で完結させる。
         *
         * @param context コンテキスト
         * @return このActivityを起動するためのIntent
         */
        fun makeIntent(context: Context): Intent {
            return Intent(context, DmcActivity::class.java)
        }
    }
}
