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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.ServerListItemBinding
import net.mm2d.dmsexplorer.domain.model.ControlPointModel
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.AttrUtils
import net.mm2d.dmsexplorer.util.FeatureUtils
import net.mm2d.dmsexplorer.view.adapter.ServerListAdapter

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerListActivityModel(
    context: Context,
    repository: Repository,
    private val serverSelectListener: ServerSelectListener,
    private val twoPane: Boolean,
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val refreshColors =
        intArrayOf(R.color.progress1, R.color.progress2, R.color.progress3, R.color.progress4)
    val progressBackground: Int =
        AttrUtils.resolveColor(context, R.attr.themeProgressBackground, Color.BLACK)
    val distanceToTriggerSync: Int =
        context.resources.getDimensionPixelOffset(R.dimen.distance_to_trigger_sync)
    val onRefreshListener: OnRefreshListener
    val serverListLayoutManager: LayoutManager
    val focusable: Boolean

    val serverListAdapter: ServerListAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val controlPointModel: ControlPointModel = repository.controlPointModel
    private val settings: Settings = Settings.get()

    private val isRefreshingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getIsRefreshingFlow(): Flow<Boolean> = isRefreshingFlow
    private val shouldShowHelpFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun getShouldShowHelpFlow(): Flow<Boolean> = shouldShowHelpFlow

    interface ServerSelectListener {
        fun onSelect(v: View)
        fun onLostSelection()
        fun onExecute(v: View)
    }

    init {
        serverListAdapter = ServerListAdapter(context, controlPointModel.mediaServerList)
        serverListAdapter.setOnItemClickListener(::onItemClick)
        serverListAdapter.setOnItemLongClickListener(::onItemLongClick)
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
        onRefreshListener = OnRefreshListener {
            setRefreshing()
            controlPointModel.restart {
                serverListAdapter.clear()
                serverListAdapter.notifyDataSetChanged()
            }
        }
        if (serverListAdapter.itemCount == 0) {
            setRefreshing()
        }
    }

    private var job: Job? = null
    private fun setRefreshing() {
        isRefreshingFlow.value = true
        shouldShowHelpFlow.value = false

        job?.cancel()
        job = scope.launch {
            delay(7000)
            if (isRefreshingFlow.value) {
                isRefreshingFlow.value = false
                shouldShowHelpFlow.value = true
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
        val server = controlPointModel.selectedMediaServer ?: return null
        val position = serverListAdapter.indexOf(server)
        if (position < 0) {
            return null
        }
        val listItem = serverListLayoutManager.findViewByPosition(position) ?: return null
        val binding = ServerListItemBinding.bind(listItem)
        return binding.accent
    }

    fun hasSelectedMediaServer(): Boolean = controlPointModel.selectedMediaServer != null

    private fun onItemClick(v: View, server: MediaServer) {
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

    private fun onItemLongClick(v: View, server: MediaServer) {
        serverListAdapter.setSelectedServer(server)
        controlPointModel.selectedMediaServer = server
        if (settings.shouldShowDeviceDetailOnTap()) {
            serverSelectListener.onExecute(v)
        } else {
            serverSelectListener.onSelect(v)
        }
    }

    private fun onDiscoverServer(server: MediaServer) {
        isRefreshingFlow.value = false
        shouldShowHelpFlow.value = false
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
