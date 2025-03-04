/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate

import android.app.ActivityOptions
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.observe
import net.mm2d.dmsexplorer.view.ContentListActivity
import net.mm2d.dmsexplorer.view.WebViewActivity
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.viewmodel.ServerListActivityModel
import net.mm2d.dmsexplorer.viewmodel.ServerListActivityModel.ServerSelectListener
import java.util.Locale

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class ServerListActivityDelegate internal constructor(
    protected val activity: BaseActivity,
    val binding: ServerListActivityBinding,
) : ServerSelectListener {

    protected abstract val isTwoPane: Boolean
    protected lateinit var model: ServerListActivityModel

    @CallSuper
    open fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        val binding = binding
        val activity = activity
        binding.toolbar.popupTheme = Settings.get().themeParams.popupThemeId
        model = ServerListActivityModel(activity, Repository.get(), this, isTwoPane)
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setTitle(R.string.title_device_select)

        binding.swipeRefreshLayout.setColorSchemeResources(*model.refreshColors)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(model.distanceToTriggerSync)
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(model.progressBackground)
        binding.swipeRefreshLayout.setOnRefreshListener(model.onRefreshListener)
        model.getIsRefreshingFlow().observe(activity) { binding.swipeRefreshLayout.isRefreshing = it }
        model.getShouldShowHelpFlow().observe(activity) { binding.help.isVisible = it }
        binding.recyclerView.adapter = model.serverListAdapter
        binding.recyclerView.layoutManager = model.serverListLayoutManager
        binding.help.setOnClickListener {
            val lang = ConfigurationCompat.getLocales(activity.resources.configuration).get(0)
            val url = if (lang == Locale.JAPAN) Const.URL_HELP_JA else Const.URL_HELP
            WebViewActivity.start(activity, "HELP", url)
        }

        val padding = activity.resources.getDimensionPixelSize(
            if (model.focusable) R.dimen.list_scale_padding else R.dimen.list_non_scale_padding,
        )
        if (binding.serverDetailContainer != null) {
            binding.recyclerView.updatePaddingRelative(start = padding)
        } else {
            binding.recyclerView.updatePaddingRelative(start = padding, end = padding)
        }

        if (savedInstanceState != null) {
            restoreScroll(savedInstanceState)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = systemBars.top }
            insets
        }
    }

    open fun prepareSaveInstanceState() {}

    @CallSuper
    open fun onSaveInstanceState(
        outState: Bundle,
    ) {
        saveScroll(outState)
    }

    open fun onStart() {}

    private fun restoreScroll(
        savedInstanceState: Bundle,
    ) {
        val position = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0)
        val offset = savedInstanceState.getInt(KEY_SCROLL_OFFSET, 0)
        if (position == 0 && offset == 0) {
            return
        }
        val recyclerView = binding.recyclerView
        recyclerView.doOnLayout {
            recyclerView.scrollToPosition(position)
            recyclerView.post { recyclerView.scrollBy(0, offset) }
        }
    }

    private fun saveScroll(
        outState: Bundle,
    ) {
        val recyclerView = binding.recyclerView
        if (recyclerView.childCount == 0) {
            return
        }
        val view = recyclerView.getChildAt(0)
        outState.putInt(KEY_SCROLL_POSITION, recyclerView.getChildAdapterPosition(view))
        outState.putInt(KEY_SCROLL_OFFSET, -view.top)
    }

    companion object {
        private const val KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION"
        private const val KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET"

        fun create(
            activity: BaseActivity,
        ): ServerListActivityDelegate {
            val binding = ServerListActivityBinding.inflate(activity.layoutInflater)
            activity.setContentView(binding.root)

            return if (binding.serverDetailContainer == null) {
                ServerListActivityDelegateOnePane(activity, binding)
            } else {
                ServerListActivityDelegateTwoPane(activity, binding)
            }
        }

        fun startCdsListActivity(
            context: Context,
            v: View,
        ) {
            val intent = ContentListActivity.makeIntent(context)
            val option = ActivityOptions
                .makeScaleUpAnimation(v, 0, 0, v.width, v.height)
                .toBundle()
            context.startActivity(intent, option)
            EventLogger.sendSelectServer()
        }
    }
}
