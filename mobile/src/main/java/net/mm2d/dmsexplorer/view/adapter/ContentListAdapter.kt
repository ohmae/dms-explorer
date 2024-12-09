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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.ContentListItemBinding
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.util.FeatureUtils
import net.mm2d.dmsexplorer.viewmodel.ContentItemModel

/**
 * CDSのコンテンツリストをRecyclerViewへ表示するためのAdapter。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentListAdapter(
    context: Context,
) : RecyclerView.Adapter<ContentListAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var clickListener: ((View, ContentEntity) -> Unit)? = null
    private var longClickListener: ((View, ContentEntity) -> Unit)? = null
    private var selectedEntity: ContentEntity? = null
    private val hasTouchScreen: Boolean = FeatureUtils.hasTouchScreen(context)
    private val translationZ: Float =
        context.resources.getDimension(R.dimen.list_item_focus_elevation)

    var list = emptyList<ContentEntity>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder = ViewHolder(ContentListItemBinding.inflate(inflater, parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.applyItem(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun setOnItemClickListener(
        listener: ((View, ContentEntity) -> Unit)?,
    ) {
        clickListener = listener
    }

    fun setOnItemLongClickListener(
        listener: ((View, ContentEntity) -> Unit)?,
    ) {
        longClickListener = listener
    }

    fun indexOf(
        entity: ContentEntity,
    ): Int = list.indexOf(entity)

    fun getSelectedEntity(): ContentEntity? = selectedEntity

    fun setSelectedEntity(
        entity: ContentEntity?,
    ): Boolean {
        if (selectedEntity != null && selectedEntity == entity) {
            return false
        }
        val previous = selectedEntity
        selectedEntity = entity
        notifyItemChangedIfPossible(previous)
        notifyItemChangedIfPossible(entity)
        return true
    }

    private fun notifyItemChangedIfPossible(
        entity: ContentEntity?,
    ) {
        if (entity == null) {
            return
        }
        val position = list.indexOf(entity)
        if (position < 0) {
            return
        }
        notifyItemChanged(position)
    }

    fun clearSelectedEntity() {
        setSelectedEntity(null)
    }

    inner class ViewHolder(
        private val binding: ContentListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private var contentEntity: ContentEntity? = null

        init {
            itemView.setOnClickListener(::onClick)
            itemView.setOnLongClickListener(::onLongClick)
            if (!hasTouchScreen) {
                itemView.onFocusChangeListener =
                    View.OnFocusChangeListener { v, focus -> this.onFocusChange(v, focus) }
            }
        }

        fun applyItem(
            entity: ContentEntity,
        ) {
            contentEntity = entity
            val selected = entity == selectedEntity
            itemView.isSelected = selected
            val model = ContentItemModel(itemView.context, entity, selected)
            binding.mark.isInvisible = !model.selected
            binding.textAccent.text = model.accentText
            binding.textAccent.background = model.accentBackground
            binding.textTitle.text = model.title
            binding.textDescription.text = model.description
            binding.textDescription.isVisible = model.hasDescription
            binding.image.setImageResource(model.imageResource)
            binding.lock.isVisible = model.isProtected
        }

        private fun onClick(
            v: View,
        ) {
            contentEntity?.let {
                clickListener?.invoke(v, it)
            }
        }

        private fun onLongClick(
            v: View,
        ): Boolean {
            contentEntity?.let {
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
