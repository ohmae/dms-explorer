/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.MusicActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.RepeatIntroductionUtils
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.MusicActivityModel

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
        val binding: MusicActivityBinding = DataBindingUtil
            .setContentView(this, R.layout.music_activity)
        try {
            model = MusicActivityModel(this, repository)
            binding.model = model
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
