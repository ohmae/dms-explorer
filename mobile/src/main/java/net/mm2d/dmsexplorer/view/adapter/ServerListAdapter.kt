/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.ServerListItemBinding
import net.mm2d.dmsexplorer.util.FeatureUtils
import net.mm2d.dmsexplorer.viewmodel.ServerItemModel

/**
 * MediaServerをRecyclerViewへ表示するためのAdapter。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ServerListAdapter(
    private val context: Context,
    servers: Collection<MediaServer>?
) : RecyclerView.Adapter<ServerListAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val list: MutableList<MediaServer> = servers?.let { ArrayList(it) } ?: ArrayList()
    private var clickListener: OnItemClickListener = ON_ITEM_CLICK_LISTENER
    private var longClickListener: OnItemLongClickListener = ON_ITEM_LONG_CLICK_LISTENER
    private var selectedServer: MediaServer? = null
    private val hasTouchScreen: Boolean = FeatureUtils.hasTouchScreen(context)
    private val translationZ: Float =
        context.resources.getDimension(R.dimen.list_item_focus_elevation)

    interface OnItemClickListener {
        fun onItemClick(
            v: View,
            server: MediaServer
        )
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(
            v: View,
            server: MediaServer
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(inflater, R.layout.server_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.applyItem(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun indexOf(server: MediaServer): Int {
        return list.indexOf(server)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        clickListener = listener ?: ON_ITEM_CLICK_LISTENER
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        longClickListener = listener ?: ON_ITEM_LONG_CLICK_LISTENER
    }

    fun clear() {
        list.clear()
    }

    fun addAll(servers: Collection<MediaServer>) {
        list.addAll(servers)
    }

    fun add(server: MediaServer): Int {
        list.add(server)
        return list.size - 1
    }

    fun remove(server: MediaServer): Int {
        val position = list.indexOf(server)
        if (position >= 0) {
            list.removeAt(position)
        }
        return position
    }

    fun setSelectedServer(server: MediaServer?) {
        if (selectedServer != null && selectedServer == server) {
            return
        }
        val previous = selectedServer
        selectedServer = server
        notifyItemChangedIfPossible(previous)
        notifyItemChangedIfPossible(server)
    }

    private fun notifyItemChangedIfPossible(server: MediaServer?) {
        server ?: return
        val position = list.indexOf(server)
        if (position < 0) {
            return
        }
        notifyItemChanged(position)
    }

    fun clearSelectedServer() {
        setSelectedServer(null)
    }

    inner class ViewHolder(
        private val binding: ServerListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var mediaServer: MediaServer? = null

        init {
            itemView.setOnClickListener(::onClick)
            itemView.setOnLongClickListener(::onLongClick)
            if (!hasTouchScreen) {
                itemView.onFocusChangeListener =
                    View.OnFocusChangeListener { v, focus -> this.onFocusChange(v, focus) }
            }
        }

        fun applyItem(server: MediaServer) {
            mediaServer = server
            val selected = server == selectedServer
            itemView.isSelected = selected
            binding.model = ServerItemModel(context, server, selected)
            binding.executePendingBindings()
        }

        private fun onClick(v: View) {
            mediaServer?.let {
                clickListener.onItemClick(v, it)
            }
        }

        private fun onLongClick(v: View): Boolean {
            mediaServer?.let {
                longClickListener.onItemLongClick(v, it)
            }
            return true
        }

        private fun onFocusChange(
            v: View,
            focus: Boolean
        ) {
            if (focus) {
                v.scaleX = FOCUS_SCALE
                v.scaleY = FOCUS_SCALE
            } else {
                v.scaleX = 1.0f
                v.scaleY = 1.0f
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                v.translationZ = if (focus) translationZ else 0.0f
            }
        }
    }

    companion object {
        private const val FOCUS_SCALE = 1.1f
        private val ON_ITEM_CLICK_LISTENER = object : OnItemClickListener {
            override fun onItemClick(v: View, server: MediaServer) {
            }
        }
        private val ON_ITEM_LONG_CLICK_LISTENER = object : OnItemLongClickListener {
            override fun onItemLongClick(v: View, server: MediaServer) {
            }
        }
    }
}
