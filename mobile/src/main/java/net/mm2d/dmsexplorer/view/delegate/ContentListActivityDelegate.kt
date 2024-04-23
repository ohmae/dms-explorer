/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.delegate

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.core.view.doOnLayout
import androidx.core.view.updatePaddingRelative
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ContentListActivityBinding
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.observe
import net.mm2d.dmsexplorer.view.base.BaseActivity
import net.mm2d.dmsexplorer.view.dialog.DeleteDialog.OnDeleteListener
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel
import net.mm2d.dmsexplorer.viewmodel.ContentListActivityModel.CdsSelectListener
import net.mm2d.dmsexplorer.viewmodel.adapter.RecyclerViewBindingAdapter

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class ContentListActivityDelegate internal constructor(
    protected val activity: BaseActivity,
    val binding: ContentListActivityBinding,
) : CdsSelectListener, OnDeleteListener {
    protected var model: ContentListActivityModel? = null
        private set

    protected abstract val isTwoPane: Boolean

    @CallSuper
    fun onCreate(savedInstanceState: Bundle?) {
        val activity = activity
        val repository = Repository.get()
        val model = try {
            ContentListActivityModel(activity, repository, this, isTwoPane)
        } catch (ignored: IllegalStateException) {
            activity.finish()
            return
        }
        this.model = model
        binding.toolbar.popupTheme = Settings.get().themeParams.popupThemeId
        binding.toolbar.setBackgroundColor(model.toolbarBackground)
        binding.toolbar.title = model.title
        model.getSubtitleFlow().observe(activity) { binding.toolbar.subtitle = it }
        binding.swipeRefreshLayout.setColorSchemeColors(*model.refreshColors)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(model.distanceToTriggerSync)
        binding.swipeRefreshLayout.setOnRefreshListener(model.onRefreshListener)
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(model.progressBackground)
        model.getIsRefreshingFlow().observe(activity) { binding.swipeRefreshLayout.isRefreshing = it }

        binding.recyclerView.itemAnimator = model.itemAnimator
        binding.recyclerView.layoutManager = model.cdsListLayoutManager
        binding.recyclerView.adapter = model.contentListAdapter
        val padding = activity.resources.getDimensionPixelSize(
            if (model.focusable) R.dimen.list_scale_padding else R.dimen.list_non_scale_padding,
        )

        if (binding.cdsDetailContainer != null) {
            binding.recyclerView.updatePaddingRelative(start = padding)
        } else {
            binding.recyclerView.updatePaddingRelative(start = padding, end = padding)
        }

        model.getScrollPositionFlow().observe(activity) {
            RecyclerViewBindingAdapter.setScrollPosition(binding.recyclerView, it)
        }
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        savedInstanceState?.let { restoreScroll(it) }
        repository.themeModel.setThemeColor(activity, model.toolbarBackground, 0)
    }

    @CallSuper
    fun onSaveInstanceState(outState: Bundle) {
        model ?: return
        saveScroll(outState)
    }

    @CallSuper
    open fun onStart() {
        model?.syncSelectedEntity()
    }

    private fun restoreScroll(savedInstanceState: Bundle) {
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

    private fun saveScroll(outState: Bundle) {
        val recyclerView = binding.recyclerView
        if (recyclerView.childCount == 0) {
            return
        }
        val view = recyclerView.getChildAt(0)
        outState.putInt(KEY_SCROLL_POSITION, recyclerView.getChildAdapterPosition(view))
        outState.putInt(KEY_SCROLL_OFFSET, -view.top)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sort) {
            model?.onSortMenuClicked()
            return true
        }
        return false
    }

    fun onBackPressed(): Boolean {
        return model?.onBackPressed() == true
    }

    fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            model?.terminate()
            return true
        }
        return false
    }

    companion object {
        private const val KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION"
        private const val KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET"

        fun create(activity: BaseActivity): ContentListActivityDelegate {
            val binding = ContentListActivityBinding.inflate(activity.layoutInflater)
            activity.setContentView(binding.root)

            return if (binding.cdsDetailContainer == null) {
                ContentListActivityDelegateOnePane(activity, binding)
            } else {
                ContentListActivityDelegateTwoPane(
                    activity,
                    binding,
                )
            }
        }
    }
}
