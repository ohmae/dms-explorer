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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.android.upnp.avt.MediaRenderer
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.RendererListItemBinding
import net.mm2d.dmsexplorer.viewmodel.RendererItemModel
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class RendererListAdapter(
    private val context: Context,
    renderers: Collection<MediaRenderer>?
) : RecyclerView.Adapter<RendererListAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val list: MutableList<MediaRenderer> = renderers?.let { ArrayList(it) } ?: ArrayList()
    private var clickListener: ((View, MediaRenderer) -> Unit)? = null
    private var longClickListener: ((View, MediaRenderer) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                inflater,
                R.layout.renderer_list_item,
                parent,
                false
            )
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

    fun indexOf(renderer: MediaRenderer): Int {
        return list.indexOf(renderer)
    }

    fun setOnItemClickListener(listener: ((View, MediaRenderer) -> Unit)?) {
        clickListener = listener
    }

    fun setOnItemLongClickListener(listener: ((View, MediaRenderer) -> Unit)?) {
        longClickListener = listener
    }

    fun clear() {
        list.clear()
    }

    fun addAll(renderers: Collection<MediaRenderer>) {
        list.addAll(renderers)
    }

    fun add(renderer: MediaRenderer): Int {
        list.add(renderer)
        return list.size - 1
    }

    fun remove(renderer: MediaRenderer): Int {
        val position = list.indexOf(renderer)
        if (position >= 0) {
            list.removeAt(position)
        }
        return position
    }

    inner class ViewHolder(
        private val binding: RendererListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var mediaRenderer: MediaRenderer? = null

        init {
            itemView.setOnClickListener(::onClick)
            itemView.setOnLongClickListener(::onLongClick)
        }

        fun applyItem(renderer: MediaRenderer) {
            mediaRenderer = renderer
            binding.model = RendererItemModel(context, renderer)
            binding.executePendingBindings()
        }

        private fun onClick(v: View) {
            mediaRenderer?.let { clickListener?.invoke(v, it) }
        }

        private fun onLongClick(v: View): Boolean {
            mediaRenderer?.let { longClickListener?.invoke(v, it) }
            return true
        }
    }
}
