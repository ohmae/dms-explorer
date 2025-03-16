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
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.DmcActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.observe
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.DmcActivityModel

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class DmcActivity : BaseActivity() {
    private var model: DmcActivityModel? = null

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
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
            model.getChapterListFlow().observe(this) { binding.seekBar.chapterList = it }
            model.getIsSeekableFlow().observe(this) { binding.seekBar.isEnabled = it }
            model.getDurationFlow().observe(this) { binding.seekBar.max = it }
            model.getProgressFlow().observe(this) { binding.seekBar.progress = it }
            binding.seekBar.setScrubBarListener(model.seekBarListener)
            model.getProgressTextFlow().observe(this) { binding.textProgress.text = it }
            model.getIsChapterInfoEnabledFlow().observe(this) {
                binding.previous.isInvisible = !it
                binding.next.isInvisible = !it
            }
            binding.previous.setOnClickListener { model.onClickPrevious() }
            binding.next.setOnClickListener { model.onClickNext() }
            model.getPlayButtonResIdFlow().observe(this) { binding.play.setImageResource(it) }
            binding.play.isInvisible = !model.isPlayControlEnabled
            binding.play.setOnClickListener { model.onClickPlay() }
            model.getIsPreparedFlow().observe(this) { binding.play.isEnabled = it }
            model.getDurationTextFlow().observe(this) { binding.textDuration.text = it }
            model.getScrubTextFlow().observe(this) { binding.textScrub.text = it }
        } catch (ignored: IllegalStateException) {
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            insets
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
        fun makeIntent(
            context: Context,
        ): Intent = Intent(context, DmcActivity::class.java)
    }
}
