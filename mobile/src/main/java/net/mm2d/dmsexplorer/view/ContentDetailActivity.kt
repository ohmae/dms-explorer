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
import android.view.View
import androidx.databinding.DataBindingUtil
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ContentDetailFragmentBinding
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.model.MediaServerModel
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.view.dialog.DeleteDialog.OnDeleteListener

/**
 * CDSアイテムの詳細情報を表示するActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentDetailActivity : BaseActivity(true), OnDeleteListener {
    private lateinit var settings: Settings
    private lateinit var mediaServerModel: MediaServerModel
    private var contentEntity: ContentEntity? = null
    private val selectedEntity: ContentEntity?
        get() = mediaServerModel.selectedEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings.get()
        setTheme(settings.themeParams.noActionBarThemeId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_detail_activity)
        val repository = Repository.get()
        mediaServerModel = repository.mediaServerModel!!
        val binding: ContentDetailFragmentBinding? =
            DataBindingUtil.findBinding(findViewById<View>(R.id.cds_detail_fragment))
        if (binding == null) {
            finish()
            return
        }
        binding.cdsDetailToolbar.popupTheme = settings.themeParams.popupThemeId
        contentEntity = selectedEntity
        setSupportActionBar(binding.cdsDetailToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val model = binding.model
        if (model != null) {
            repository.themeModel.setThemeColor(this, model.collapsedColor, 0)
        }
    }

    override fun updateOrientationSettings() {
        settings.browseOrientation
            .setRequestedOrientation(this)
    }

    override fun onStart() {
        super.onStart()
        if (contentEntity != null && contentEntity != selectedEntity) {
            finish()
        }
    }

    override fun onDelete() {
        finish()
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
            return Intent(context, ContentDetailActivity::class.java)
        }
    }
}
