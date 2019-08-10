/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.databinding.PropertyListItemBinding
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter.ViewHolder
import net.mm2d.dmsexplorer.viewmodel.PropertyItemModel
import java.util.*
import java.util.regex.Pattern

/**
 * 詳細情報の各項目をRecyclerViewを使用して表示するためのAdapter。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class PropertyAdapter(
    protected val context: Context
) : Adapter<ViewHolder>() {
    private val list: MutableList<Entry> = ArrayList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    @JvmOverloads
    fun addEntry(name: String, value: String?, type: Type = Type.TEXT) {
        if (value.isNullOrEmpty()) {
            return
        }
        list.add(Entry(name, value, type))
    }

    fun addTitleEntry(name: String) {
        list.add(Entry(name, "", Type.TITLE))
    }

    override fun getItemViewType(position: Int): Int =
        if (list[position].type != Type.DESCRIPTION) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        context, inflater,
        DataBindingUtil.inflate(inflater, R.layout.property_list_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.applyItem(list[position])
    }

    override fun getItemCount(): Int = list.size

    enum class Type {
        TITLE,
        TEXT,
        LINK,
        DESCRIPTION
    }

    class Entry(
        val name: String,
        val value: String,
        val type: Type
    )

    class ViewHolder(
        private val context: Context,
        private val inflater: LayoutInflater,
        private val binding: PropertyListItemBinding
    ) : RecyclerView.ViewHolder(binding.root), OnClickListener {

        fun applyItem(entry: Entry) {
            var value = entry.value
            if (entry.type == Type.DESCRIPTION) {
                value = setUpDescription(entry)
            }
            binding.model = PropertyItemModel(entry.name, entry.type, value, this)
            binding.executePendingBindings()
        }

        private fun setUpDescription(entry: Entry): String {
            val layout = binding.container
            val count = layout.childCount
            for (i in count - 1 downTo 2) {
                layout.removeViewAt(i)
            }
            return makeDescription(layout, entry.value)
        }

        private fun makeDescription(parent: ViewGroup, text: String): String {
            var firstNormalText = ""
            val matcher = URL_PATTERN.matcher(text)
            var lastEnd = 0
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                if (start != lastEnd) {
                    val normalText = text.substring(lastEnd, start).trim { isSpace(it) }
                    if (normalText.isNotEmpty()) {
                        if (lastEnd == 0) {
                            firstNormalText = normalText
                        } else {
                            addNormalText(parent, normalText)
                        }
                    }
                }
                addLinkText(parent, text.substring(start, end).trim { isSpace(it) })
                lastEnd = end
            }
            if (lastEnd == 0) {
                return text.trim { isSpace(it) }
            } else if (lastEnd != text.length) {
                val normalText = text.substring(lastEnd, text.length).trim { isSpace(it) }
                if (normalText.isNotEmpty()) {
                    addNormalText(parent, normalText)
                }
            }
            return firstNormalText
        }

        private fun addNormalText(parent: ViewGroup, text: String) {
            val normal = inflater.inflate(R.layout.normal_text_view, parent, false) as TextView
            normal.text = text
            parent.addView(normal)
        }

        private fun addLinkText(parent: ViewGroup, text: String) {
            val link = inflater.inflate(R.layout.link_text_view, parent, false) as TextView
            link.text = text
            link.paintFlags = link.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            link.setOnClickListener(this)
            parent.addView(link)
        }

        override fun onClick(v: View) {
            val text = (v as? TextView)?.text
            if (text.isNullOrEmpty()) {
                return
            }
            Repository.get().openUriModel.openUri(context, text.toString())
        }
    }

    companion object {
        @JvmStatic
        fun ofServer(context: Context, server: MediaServer): PropertyAdapter =
            ServerPropertyAdapter(context, server)

        @JvmStatic
        fun ofContent(context: Context, entity: ContentEntity): PropertyAdapter =
            ContentPropertyAdapter(context, entity)

        private val URL_PATTERN = Pattern.compile("https?://[\\w/:%#$&?()~.=+\\-]+")

        private fun isSpace(c: Char): Boolean = c <= '\u0020' || c == '\u00A0' || c == '\u3000'
    }
}
