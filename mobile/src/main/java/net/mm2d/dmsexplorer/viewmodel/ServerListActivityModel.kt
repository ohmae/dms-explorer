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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener
import net.mm2d.dmsexplorer.BR
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ServerListItemBinding
import net.mm2d.dmsexplorer.domain.model.ControlPointModel
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.AttrUtils
import net.mm2d.dmsexplorer.util.FeatureUtils
import net.mm2d.dmsexplorer.view.adapter.ServerListAdapter
import net.mm2d.dmsexplorer.view.animator.CustomItemAnimator

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerListActivityModel(
    context: Context,
    repository: Repository,
    private val serverSelectListener: ServerSelectListener,
    private val twoPane: Boolean
) : BaseObservable() {
    val refreshColors =
        intArrayOf(R.color.progress1, R.color.progress2, R.color.progress3, R.color.progress4)
    val progressBackground: Int =
        AttrUtils.resolveColor(context, R.attr.themeProgressBackground, Color.BLACK)
    val distanceToTriggerSync: Int =
        context.resources.getDimensionPixelOffset(R.dimen.distance_to_trigger_sync)
    val onRefreshListener: OnRefreshListener
    val itemAnimator: ItemAnimator
    val serverListLayoutManager: LayoutManager
    val focusable: Boolean

    val serverListAdapter: ServerListAdapter
    private var _refreshing: Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    private val controlPointModel: ControlPointModel = repository.controlPointModel
    private val settings: Settings = Settings.get()

    var isRefreshing: Boolean
        @Bindable
        get() = _refreshing
        set(refreshing) {
            _refreshing = refreshing
            notifyPropertyChanged(BR.refreshing)
        }

    interface ServerSelectListener {
        fun onSelect(v: View)

        fun onLostSelection()

        fun onExecute(v: View)
    }

    init {
        serverListAdapter = ServerListAdapter(context, controlPointModel.mediaServerList)
        serverListAdapter.setOnItemClickListener(::onItemClick)
        serverListAdapter.setOnItemLongClickListener(::onItemLongClick)
        _refreshing = serverListAdapter.itemCount == 0
        controlPointModel.setMsDiscoveryListener(object : MsDiscoveryListener {
            override fun onDiscover(server: MediaServer) {
                handler.post { onDiscoverServer(server) }
            }

            override fun onLost(server: MediaServer) {
                handler.post { onLostServer(server) }
            }
        })

        focusable = !FeatureUtils.hasTouchScreen(context)
        serverListLayoutManager = LinearLayoutManager(context)
        itemAnimator = CustomItemAnimator(context)
        onRefreshListener = OnRefreshListener {
            controlPointModel.restart {
                serverListAdapter.clear()
                serverListAdapter.notifyDataSetChanged()
            }
        }
    }

    fun updateListAdapter() {
        serverListAdapter.clear()
        serverListAdapter.addAll(controlPointModel.mediaServerList)
        serverListAdapter.notifyDataSetChanged()
        serverListAdapter.setSelectedServer(controlPointModel.selectedMediaServer)
    }

    fun findSharedView(): View? {
        val server = controlPointModel.selectedMediaServer
        val position = serverListAdapter.indexOf(server!!)
        if (position < 0) {
            return null
        }
        val listItem = serverListLayoutManager.findViewByPosition(position)
        val binding = DataBindingUtil.findBinding<ServerListItemBinding>(listItem!!)
        return binding?.accent
    }

    fun hasSelectedMediaServer(): Boolean {
        return controlPointModel.selectedMediaServer != null
    }

    private fun onItemClick(
        v: View,
        server: MediaServer
    ) {
        val alreadySelected = controlPointModel.isSelectedMediaServer(server)
        serverListAdapter.setSelectedServer(server)
        controlPointModel.selectedMediaServer = server
        if (settings.shouldShowDeviceDetailOnTap()) {
            if (twoPane && alreadySelected) {
                serverSelectListener.onExecute(v)
            } else {
                serverSelectListener.onSelect(v)
            }
        } else {
            serverSelectListener.onExecute(v)
        }
    }

    private fun onItemLongClick(
        v: View,
        server: MediaServer
    ) {
        serverListAdapter.setSelectedServer(server)
        controlPointModel.selectedMediaServer = server

        if (settings.shouldShowDeviceDetailOnTap()) {
            serverSelectListener.onExecute(v)
        } else {
            serverSelectListener.onSelect(v)
        }
    }

    private fun onDiscoverServer(server: MediaServer) {
        isRefreshing = false
        if (controlPointModel.numberOfMediaServer == serverListAdapter.itemCount + 1) {
            val position = serverListAdapter.add(server)
            serverListAdapter.notifyItemInserted(position)
        } else {
            serverListAdapter.clear()
            serverListAdapter.addAll(controlPointModel.mediaServerList)
            serverListAdapter.notifyDataSetChanged()
        }
    }

    private fun onLostServer(server: MediaServer) {
        val position = serverListAdapter.remove(server)
        if (position < 0) {
            return
        }
        if (controlPointModel.numberOfMediaServer == serverListAdapter.itemCount) {
            serverListAdapter.notifyItemRemoved(position)
        } else {
            serverListAdapter.clear()
            serverListAdapter.addAll(controlPointModel.mediaServerList)
            serverListAdapter.notifyDataSetChanged()
        }
        if (server == controlPointModel.selectedMediaServer) {
            serverSelectListener.onLostSelection()
            serverListAdapter.clearSelectedServer()
            controlPointModel.clearSelectedServer()
        }
    }
}
