/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import net.mm2d.dmsexplorer.BR
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

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentListActivityModel(
    context: Context,
    repository: Repository,
    private val cdsSelectListener: CdsSelectListener,
    private val twoPane: Boolean
) : BaseObservable(), ExploreListener {
    val refreshColors =
        intArrayOf(R.color.progress1, R.color.progress2, R.color.progress3, R.color.progress4)
    val progressBackground: Int
    val distanceToTriggerSync: Int
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
    private val settings: Settings
    private var updateList: Runnable = Runnable { }
    private var updateTime: Long = 0
    private var timerResetLatch: Boolean = false

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
            entity: ContentEntity
        )

        fun onLostSelection()

        fun onExecute(
            v: View,
            entity: ContentEntity,
            selected: Boolean
        )
    }

    init {
        progressBackground =
            AttrUtils.resolveColor(context, R.attr.themeProgressBackground, Color.BLACK)
        distanceToTriggerSync =
            context.resources.getDimensionPixelOffset(R.dimen.distance_to_trigger_sync)
        settings = Settings.get()
        val model = repository.mediaServerModel ?: throw IllegalStateException()
        mediaServerModel = model
        mediaServerModel.setExploreListener(this)
        contentListAdapter = ContentListAdapter(context)
        contentListAdapter.setOnItemClickListener(::onItemClick)
        contentListAdapter.setOnItemLongClickListener(::onItemLongClick)

        focusable = !FeatureUtils.hasTouchScreen(context)
        cdsListLayoutManager = LinearLayoutManager(context)
        itemAnimator = CustomItemAnimator(context)
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
        entity: ContentEntity
    ) {
        if (mediaServerModel.enterChild(entity)) {
            cdsSelectListener.onLostSelection()
            return
        }
        val selected = entity == mediaServerModel.selectedEntity
        mediaServerModel.selectedEntity = entity
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
        entity: ContentEntity
    ) {
        if (mediaServerModel.enterChild(entity)) {
            cdsSelectListener.onLostSelection()
            return
        }
        val selected = entity == mediaServerModel.selectedEntity
        mediaServerModel.selectedEntity = entity
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

    private fun calculateDelay(
        before: Int,
        after: Int
    ): Long {
        if (before > after) {
            return 0
        }
        val diff = System.currentTimeMillis() - updateTime
        if (before == 0) {
            return if (after < FIRST_COUNT && diff < FIRST_INTERVAL) {
                FIRST_INTERVAL - diff
            } else 0
        }
        return if (after - before < SECOND_COUNT && diff < SECOND_INTERVAL) {
            SECOND_INTERVAL - diff
        } else 0
    }

    private fun updateList(list: List<ContentEntity>) {
        val beforeSize = contentListAdapter.itemCount
        val afterSize = list.size
        if (beforeSize == afterSize) {
            return
        }
        if (timerResetLatch && beforeSize == 0 && afterSize == 1) {
            updateTime = System.currentTimeMillis()
        }
        timerResetLatch = afterSize == 0
        handler.removeCallbacks(updateList)
        val delay = calculateDelay(beforeSize, afterSize)
        if (delay > 0) {
            updateList = Runnable { updateList(list) }
            handler.postDelayed(updateList, delay)
            return
        }
        updateTime = System.currentTimeMillis()
        val entity = mediaServerModel.selectedEntity
        contentListAdapter.clear()
        contentListAdapter.addAll(list)
        contentListAdapter.setSelectedEntity(entity)
        if (beforeSize < afterSize) {
            contentListAdapter.notifyItemRangeInserted(beforeSize, afterSize - beforeSize)
        } else {
            contentListAdapter.notifyDataSetChanged()
        }
        if (beforeSize == 0 && entity != null) {
            scrollPosition = list.indexOf(entity)
        } else {
            _scrollPosition = INVALID_POSITION
        }
    }

    fun terminate() {
        mediaServerModel.terminate()
        mediaServerModel.initialize()
    }

    companion object {
        private const val INVALID_POSITION = -1
        private const val FIRST_COUNT = 20
        private const val SECOND_COUNT = 300
        private const val FIRST_INTERVAL: Long = 50
        private const val SECOND_INTERVAL: Long = 300
    }
}
