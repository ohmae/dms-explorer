/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import net.mm2d.upnp.util.asIterable
import org.w3c.dom.Element
import java.util.*

/**
 * シンプルなXMLのタグ情報を表現するクラス
 *
 * Elementのままでは情報の参照コストが高いため、
 * よりシンプルな構造に格納するためのクラス。
 * CdsObjectのXMLのようにElementが入れ子になることのない
 * タグ＋値、属性＋値の情報を表現できれば十分なものを表現するのに使用する。
 * 入れ子関係を持つXMLは表現できない。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class Tag(
    val name: String,
    val value: String,
    val attributes: Map<String, String>
) : Parcelable {
    /**
     * 属性値を返す。
     *
     * @param name 属性名
     * @return 属性値、見つからない場合null
     */
    fun getAttribute(name: String?): String? {
        return attributes[name]
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(value)
        attributes.forEach {
            sb.append("\n@${it.key} => ${it.value}")
        }
        return sb.toString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(value)
        dest.writeInt(attributes.size)
        attributes.forEach {
            dest.writeString(it.key)
            dest.writeString(it.value)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Creator<Tag> {
        val EMPTY = Tag("", "", emptyMap())
        override fun createFromParcel(parcel: Parcel): Tag = create(parcel)
        override fun newArray(size: Int): Array<Tag?> = arrayOfNulls(size)

        private fun create(parcel: Parcel): Tag {
            val name = parcel.readString()!!
            val value = parcel.readString()!!
            val size = parcel.readInt()
            val map = LinkedHashMap<String, String>(size)
            for (i in 0 until size) {
                map[parcel.readString()!!] = parcel.readString()!!
            }
            return Tag(name, value, map)
        }

        /**
         * インスタンス作成。
         *
         * パッケージ外でのインスタンス化禁止
         *
         * @param element タグ情報
         * @param root    タグがitem/containerのときtrue
         */
        @JvmOverloads
        fun create(element: Element, root: Boolean = false): Tag =
            create(element, if (root) "" else element.textContent)

        /**
         * インスタンス作成。
         *
         * @param element タグ情報
         * @param value   タグの値
         */
        private fun create(element: Element, value: String): Tag = Tag(
            element.tagName,
            value,
            element.attributes.asIterable().map { it.nodeName to it.nodeValue }.toMap()
        )
    }
}
