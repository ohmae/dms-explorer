/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.model.ControlPointModel
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.AttrUtils
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.view.delegate.ServerListActivityDelegate

/**
 * MediaServerのサーチ、選択を行うActivity。
 *
 *
 * アプリ起動時最初に表示されるActivity
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerListActivity : BaseActivity(true) {
    private lateinit var settings: Settings
    private lateinit var controlPointModel: ControlPointModel
    private lateinit var delegate: ServerListActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings.get()
        setTheme(settings.themeParams.listThemeId)
        super.onCreate(savedInstanceState)
        val repository = Repository.get()
        controlPointModel = repository.controlPointModel

        repository.themeModel.setThemeColor(
            this,
            AttrUtils.resolveColor(this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK),
            ContextCompat.getColor(this, R.color.defaultStatusBar),
        )

        if (savedInstanceState == null) {
            controlPointModel.initialize()
        }
        delegate = ServerListActivityDelegate.create(this)
        delegate.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            controlPointModel.terminate()
        }
    }

    override fun updateOrientationSettings() {
        settings.browseOrientation
            .setRequestedOrientation(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        delegate.prepareSaveInstanceState()
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        controlPointModel.searchStart()
        delegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        controlPointModel.searchStop()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ServerListActivity::class.java)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
