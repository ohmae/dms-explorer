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
import android.view.KeyEvent
import android.view.MenuItem
import androidx.core.view.WindowCompat
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.view.delegate.ContentListActivityDelegate
import net.mm2d.dmsexplorer.view.dialog.DeleteDialog.OnDeleteListener

/**
 * MediaServerのContentDirectoryを表示、操作するActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentListActivity :
    BaseActivity(true, R.menu.content_list),
    OnDeleteListener {
    private lateinit var settings: Settings
    private lateinit var delegate: ContentListActivityDelegate

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        settings = Settings.get()
        setTheme(settings.themeParams.listThemeId)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        delegate = ContentListActivityDelegate.create(this)
        delegate.onCreate(savedInstanceState)
    }

    override fun updateOrientationSettings() {
        settings.browseOrientation
            .setRequestedOrientation(this)
    }

    override fun onSaveInstanceState(
        outState: Bundle,
    ) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onOptionsItemSelected(
        item: MenuItem,
    ): Boolean = delegate.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onBackPressed() {
        if (delegate.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onKeyLongPress(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        if (delegate.onKeyLongPress(keyCode, event)) {
            super.onBackPressed()
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun navigateUpTo() {
        onBackPressed()
    }

    override fun onDelete() {
        delegate.onDelete()
    }

    companion object {
        /**
         * このActivityを起動するためのIntentを作成する。
         *
         *
         * Extraの設定と読み出しをこのクラス内で完結させる。
         *
         * @param context コンテキスト
         * @return このActivityを起動するためのIntent
         */
        fun makeIntent(
            context: Context,
        ): Intent = Intent(context, ContentListActivity::class.java)
    }
}
