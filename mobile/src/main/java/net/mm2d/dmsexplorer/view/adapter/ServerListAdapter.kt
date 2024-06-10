/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
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
    servers: Collection<MediaServer>?,
) : RecyclerView.Adapter<ServerListAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val list: MutableList<MediaServer> = servers?.let { ArrayList(it) } ?: ArrayList()
    private var clickListener: ((View, MediaServer) -> Unit)? = null
    private var longClickListener: ((View, MediaServer) -> Unit)? = null
    private var selectedServer: MediaServer? = null
    private val hasTouchScreen: Boolean = FeatureUtils.hasTouchScreen(context)
    private val translationZ: Float = context.resources
        .getDimension(R.dimen.list_item_focus_elevation)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder = ViewHolder(ServerListItemBinding.inflate(inflater, parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.applyItem(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun indexOf(server: MediaServer): Int = list.indexOf(server)

    fun setOnItemClickListener(listener: ((View, MediaServer) -> Unit)?) {
        clickListener = listener
    }

    fun setOnItemLongClickListener(listener: ((View, MediaServer) -> Unit)?) {
        longClickListener = listener
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
        private val binding: ServerListItemBinding,
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
            val model = ServerItemModel(context, server, selected)
            binding.mark.isInvisible = !model.selected
            binding.textAccent.setBackground(model.accentBackground)
            binding.textAccent.text = model.accentText
            binding.textAccent.isInvisible = model.accentIcon != null
            binding.imageAccent.setImageBitmap(model.accentIcon)
            binding.imageAccent.isInvisible = model.accentIcon == null
            binding.textTitle.text = model.title
            binding.textDescription.text = model.description
        }

        private fun onClick(v: View) {
            mediaServer?.let {
                clickListener?.invoke(v, it)
            }
        }

        private fun onLongClick(v: View): Boolean {
            mediaServer?.let {
                longClickListener?.invoke(v, it)
            }
            return true
        }

        private fun onFocusChange(
            v: View,
            focus: Boolean,
        ) {
            if (focus) {
                v.scaleX = FOCUS_SCALE
                v.scaleY = FOCUS_SCALE
            } else {
                v.scaleX = 1.0f
                v.scaleY = 1.0f
            }
            v.translationZ = if (focus) translationZ else 0.0f
        }
    }

    companion object {
        private const val FOCUS_SCALE = 1.1f
    }
}
