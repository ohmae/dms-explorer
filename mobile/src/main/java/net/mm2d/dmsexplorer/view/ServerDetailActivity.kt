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
import android.transition.Transition
import android.view.View
import android.view.ViewAnimationUtils
import androidx.databinding.DataBindingUtil
import net.mm2d.android.view.TransitionListenerAdapter
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ServerDetailFragmentBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.view.base.BaseActivity
import kotlin.math.sqrt

/**
 * メディアサーバの詳細情報を表示するActivity。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerDetailActivity : BaseActivity(true) {
    private lateinit var settings: Settings
    private lateinit var binding: ServerDetailFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = Settings.get()
        setTheme(settings.themeParams.noActionBarThemeId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_detail_activity)
        binding = DataBindingUtil.findBinding(findViewById(R.id.server_detail_fragment))
            ?: return finish()
        binding.serverDetailToolbar.popupTheme = settings.themeParams.popupThemeId
        setSupportActionBar(binding.serverDetailToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.model?.let {
            Repository.get().themeModel.setThemeColor(this, it.collapsedColor, 0)
        }
        prepareTransition(savedInstanceState != null)
    }

    override fun updateOrientationSettings() {
        settings.browseOrientation
            .setRequestedOrientation(this)
    }

    private fun prepareTransition(hasState: Boolean) {
        binding.toolbarIcon.transitionName = Const.SHARE_ELEMENT_NAME_DEVICE_ICON
        if (hasState) {
            return
        }
        binding.toolbarBackground.visibility = View.INVISIBLE
        window.sharedElementEnterTransition.addListener(object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                transition.removeListener(this)
                startAnimation(binding.toolbarBackground)
            }
        })
    }

    private fun startAnimation(background: View) {
        if (!background.isAttachedToWindow) {
            return
        }
        background.visibility = View.VISIBLE
        val res = resources
        val iconRadius = res.getDimension(R.dimen.expanded_toolbar_icon_radius)
        val iconMargin = res.getDimension(R.dimen.expanded_toolbar_icon_margin)
        val iconCenter = iconRadius + iconMargin
        val cx = background.width - iconCenter
        val cy = background.height - iconCenter
        ViewAnimationUtils.createCircularReveal(
            background,
            cx.toInt(), cy.toInt(), iconRadius, sqrt((cx * cx + cy * cy).toDouble()).toFloat()
        ).start()
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
        fun makeIntent(context: Context): Intent =
            Intent(context, ServerDetailActivity::class.java)
    }
}
