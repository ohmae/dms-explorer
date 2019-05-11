/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import android.os.Parcel
import android.os.Parcelable.Creator
import net.mm2d.android.upnp.cds.CdsObject.Companion.AUDIO_ITEM
import net.mm2d.android.upnp.cds.CdsObject.Companion.ContentType
import net.mm2d.android.upnp.cds.CdsObject.Companion.IMAGE_ITEM
import net.mm2d.android.upnp.cds.CdsObject.Companion.TYPE_AUDIO
import net.mm2d.android.upnp.cds.CdsObject.Companion.TYPE_CONTAINER
import net.mm2d.android.upnp.cds.CdsObject.Companion.TYPE_IMAGE
import net.mm2d.android.upnp.cds.CdsObject.Companion.TYPE_UNKNOWN
import net.mm2d.android.upnp.cds.CdsObject.Companion.TYPE_VIDEO
import net.mm2d.android.upnp.cds.CdsObject.Companion.VIDEO_ITEM
import net.mm2d.upnp.util.forEachElement
import org.w3c.dom.Element
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class CdsObjectImpl(
    override val udn: String,
    override val isItem: Boolean,
    override val rootTag: Tag,
    internal val tagMap: TagMap
) : CdsObject {
    override val objectId: String = tagMap.getValue(CdsObject.ID)
        ?: throw IllegalArgumentException("Malformed item")
    override val parentId: String = tagMap.getValue(CdsObject.PARENT_ID)
        ?: throw IllegalArgumentException("Malformed item")
    override val upnpClass: String = tagMap.getValue(CdsObject.UPNP_CLASS)
        ?: throw IllegalArgumentException("Malformed item")
    override val title: String = tagMap.getValue(CdsObject.DC_TITLE)
        ?: throw IllegalArgumentException("Malformed item")
    override val type: Int = getType(isItem, upnpClass)

    override fun getValue(xpath: String, index: Int): String? {
        return tagMap.getValue(xpath, index)
    }

    override fun getValue(tagName: String?, attrName: String?, index: Int): String? {
        return tagMap.getValue(tagName, attrName, index)
    }

    override fun getTag(tagName: String?, index: Int): Tag? {
        return tagMap.getTag(tagName, index)
    }

    override fun getTagList(tagName: String?): List<Tag>? {
        return tagMap.getTagList(tagName)
    }

    override fun getIntValue(xpath: String, defaultValue: Int, index: Int): Int {
        return getValue(xpath, index)?.toIntOrNull() ?: defaultValue
    }

    override fun getDateValue(xpath: String, index: Int): Date? {
        return PropertyParser.parseDate(getValue(xpath, index))
    }

    override fun getResourceCount(): Int {
        return getTagList(CdsObject.RES)?.size ?: 0
    }

    override fun hasResource(): Boolean =
        getTagList(CdsObject.RES)?.isNotEmpty() ?: false

    override fun hasProtectedResource(): Boolean {
        return getTagList(CdsObject.RES)?.find {
            PropertyParser.extractMimeTypeFromProtocolInfo(
                it.getAttribute(CdsObject.PROTOCOL_INFO)
            ) == "application/x-dtcp1"
        } != null
    }

    override fun toString(): String = title
    override fun toDumpString(): String = tagMap.toString()
    override fun hashCode(): Int = tagMap.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CdsObject) return false
        return objectId == other.objectId && udn == other.udn
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(udn)
        dest.writeByte((if (isItem) 1 else 0).toByte())
        dest.writeParcelable(rootTag, flags)
        dest.writeParcelable(tagMap, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Creator<CdsObjectImpl> {
        override fun createFromParcel(parcel: Parcel): CdsObjectImpl {
            return create(parcel)
        }

        override fun newArray(size: Int): Array<CdsObjectImpl?> {
            return arrayOfNulls(size)
        }

        /**
         * elementをもとにインスタンス作成
         *
         * @param udn     MediaServerのUDN
         * @param element objectを示すelement
         * @param rootTag DIDL-Liteノードの情報
         */
        fun create(
            udn: String,
            element: Element,
            rootTag: Tag
        ) = CdsObjectImpl(
            udn,
            when (element.tagName) {
                CdsObject.ITEM -> true
                CdsObject.CONTAINER -> false
                else -> throw IllegalArgumentException()
            },
            rootTag,
            parseElement(element)
        )

        private fun create(parcel: Parcel) = CdsObjectImpl(
            parcel.readString()!!,
            parcel.readByte().toInt() != 0,
            parcel.readParcelable(Tag::class.java.classLoader)!!,
            parcel.readParcelable(TagMap::class.java.classLoader)!!
        )

        /**
         * 子要素の情報をパースし、格納する。
         *
         * @param element objectを示すelement
         */
        private fun parseElement(element: Element): TagMap {
            val map: MutableMap<String, MutableList<Tag>> = mutableMapOf()
            map[""] = mutableListOf(Tag.create(element, true))
            element.firstChild?.forEachElement {
                val key = it.nodeName
                if (map[key]?.add(Tag.create(it)) == null) {
                    map[key] = mutableListOf(Tag.create(it))
                }
            }
            return TagMap(map)
        }

        @ContentType
        private fun getType(
            isItem: Boolean,
            upnpClass: String
        ): Int = if (!isItem) {
            TYPE_CONTAINER
        } else if (upnpClass.startsWith(IMAGE_ITEM)) {
            TYPE_IMAGE
        } else if (upnpClass.startsWith(AUDIO_ITEM)) {
            TYPE_AUDIO
        } else if (upnpClass.startsWith(VIDEO_ITEM)) {
            TYPE_VIDEO
        } else {
            TYPE_UNKNOWN
        }
    }
}