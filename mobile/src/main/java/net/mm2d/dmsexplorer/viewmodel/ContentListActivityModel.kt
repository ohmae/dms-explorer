/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import net.mm2d.dmsexplorer.Const
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.domain.model.ExploreListener
import net.mm2d.dmsexplorer.domain.model.MediaServerModel
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.AttrUtils
import net.mm2d.dmsexplorer.util.FeatureUtils
import net.mm2d.dmsexplorer.view.adapter.ContentListAdapter
import net.mm2d.dmsexplorer.view.animator.CustomItemAnimator
import net.mm2d.dmsexplorer.view.dialog.SortDialog

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentListActivityModel(
    private val activity: FragmentActivity,
    repository: Repository,
    private val cdsSelectListener: CdsSelectListener,
    private val twoPane: Boolean,
) : BaseObservable(), ExploreListener {
    val refreshColors =
        intArrayOf(R.color.progress1, R.color.progress2, R.color.progress3, R.color.progress4)
    val progressBackground: Int =
        AttrUtils.resolveColor(activity, R.attr.themeProgressBackground, Color.BLACK)
    val distanceToTriggerSync: Int =
        activity.resources.getDimensionPixelOffset(R.dimen.distance_to_trigger_sync)
    val onRefreshListener: OnRefreshListener
    val itemAnimator: ItemAnimator
    val cdsListLayoutManager: LayoutManager
    val title: String
    val toolbarBackground: Int
    val focusable: Boolean
    val contentListAdapter: ContentListAdapter

    @get:Bindable
    var subtitle = ""
        set(subtitle) {
            field = subtitle
            notifyPropertyChanged(BR.subtitle)
        }

    @get:Bindable
    var isRefreshing: Boolean = false
        set(refreshing) {
            field = refreshing
            notifyPropertyChanged(BR.refreshing)
        }
    private var _scrollPosition = INVALID_POSITION

    private val handler = Handler(Looper.getMainLooper())
    private val mediaServerModel: MediaServerModel
    private val settings: Settings = Settings.get()

    val isItemSelected: Boolean
        get() {
            return mediaServerModel.selectedEntity?.type?.isPlayable == true
        }

    // 選択項目を中央に表示させる処理
    // FIXME: DataBindingを使ったことで返って複雑化してしまっている
    var scrollPosition: Int
        @Bindable
        get() = _scrollPosition
        set(position) {
            _scrollPosition = position
            notifyPropertyChanged(BR.scrollPosition)
        }

    interface CdsSelectListener {
        fun onSelect(
            v: View,
            entity: ContentEntity,
        )

        fun onLostSelection()

        fun onExecute(
            v: View,
            entity: ContentEntity,
            selected: Boolean,
        )
    }

    init {
        val model = repository.mediaServerModel ?: throw IllegalStateException()
        mediaServerModel = model
        mediaServerModel.setExploreListener(this)
        contentListAdapter = ContentListAdapter(activity).also {
            it.setOnItemClickListener(::onItemClick)
            it.setOnItemLongClickListener(::onItemLongClick)
        }

        focusable = !FeatureUtils.hasTouchScreen(activity)
        cdsListLayoutManager = LinearLayoutManager(activity)
        itemAnimator = CustomItemAnimator(activity)
        title = mediaServerModel.title
        onRefreshListener = OnRefreshListener { mediaServerModel.reload() }
        val server = mediaServerModel.mediaServer
        settings.themeParams
            .serverColorExtractor
            .invoke(server, null)
        toolbarBackground = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK)
    }

    private fun onItemClick(
        v: View,
        entity: ContentEntity,
    ) {
        if (mediaServerModel.enterChild(entity)) {
            cdsSelectListener.onLostSelection()
            return
        }
        val selected = entity == mediaServerModel.selectedEntity
        mediaServerModel.setSelectedEntity(entity)
        contentListAdapter.setSelectedEntity(entity)
        if (settings.shouldShowContentDetailOnTap()) {
            if (twoPane && selected) {
                cdsSelectListener.onExecute(v, entity, true)
            } else {
                cdsSelectListener.onSelect(v, entity)
            }
        } else {
            cdsSelectListener.onExecute(v, entity, selected)
        }
    }

    private fun onItemLongClick(
        v: View,
        entity: ContentEntity,
    ) {
        if (mediaServerModel.enterChild(entity)) {
            cdsSelectListener.onLostSelection()
            return
        }
        val selected = entity == mediaServerModel.selectedEntity
        mediaServerModel.setSelectedEntity(entity)
        contentListAdapter.setSelectedEntity(entity)

        if (settings.shouldShowContentDetailOnTap()) {
            cdsSelectListener.onExecute(v, entity, selected)
        } else {
            cdsSelectListener.onSelect(v, entity)
        }
    }

    fun syncSelectedEntity() {
        val entity = mediaServerModel.selectedEntity
        if (!contentListAdapter.setSelectedEntity(entity) || entity == null) {
            return
        }
        val index = contentListAdapter.indexOf(entity)
        if (index >= 0) {
            scrollPosition = index
        }
    }

    fun onSortMenuClicked() {
        SortDialog.show(activity)
    }

    fun onBackPressed(): Boolean {
        cdsSelectListener.onLostSelection()
        return mediaServerModel.exitToParent()
    }

    override fun onStart() {
        setSize(0)
        isRefreshing = true
        handler.post { updateList(emptyList()) }
    }

    override fun onUpdate(list: List<ContentEntity>) {
        setSize(list.size)
        handler.post { updateList(list) }
    }

    override fun onComplete() {
        isRefreshing = false
    }

    private fun setSize(size: Int) {
        subtitle = "[$size] ${mediaServerModel.path}"
    }

    private val scrollPositionTask = Runnable {
        scrollPosition = contentListAdapter.list.indexOf(mediaServerModel.selectedEntity)
    }

    private fun updateList(list: List<ContentEntity>) {
        val entity = mediaServerModel.selectedEntity
        val oldList = contentListAdapter.list
        val diff = DiffUtil.calculateDiff(DiffCallback(oldList, list), true)
        contentListAdapter.list = list
        contentListAdapter.setSelectedEntity(entity)
        diff.dispatchUpdatesTo(contentListAdapter)
        handler.removeCallbacks(scrollPositionTask)
        handler.postDelayed(scrollPositionTask, SCROLL_POSITION_DELAY)
    }

    private class DiffCallback(
        private val old: List<ContentEntity>,
        private val new: List<ContentEntity>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].id == new[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].name == new[newItemPosition].name
    }

    fun terminate() {
        mediaServerModel.terminate()
        mediaServerModel.initialize()
    }

    companion object {
        private const val INVALID_POSITION = -1
        private const val SCROLL_POSITION_DELAY = 200L
    }
}
