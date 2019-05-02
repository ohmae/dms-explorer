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
class Tag : Parcelable {
    private val _attribute: MutableMap<String, String>

    /**
     * タグ名を返す。
     *
     * @return タグ名
     */
    val name: String
    /**
     * タグの値を返す。
     *
     * @return タグの値
     */
    val value: String
    /**
     * 属性値を格納したMapを返す。
     *
     * @return 属性値を格納したMap
     */
    val attributes: Map<String, String>
        get() = _attribute

    /**
     * インスタンス作成。
     *
     * パッケージ外でのインスタンス化禁止
     *
     * @param element タグ情報
     * @param root    タグがitem/containerのときtrue
     */
    @JvmOverloads
    internal constructor(
        element: Element,
        root: Boolean = false
    ) : this(element, if (root) "" else element.textContent)

    /**
     * インスタンス作成。
     *
     * @param element タグ情報
     * @param value   タグの値
     */
    private constructor(
        element: Element,
        value: String
    ) {
        name = element.tagName
        this.value = value
        val attributes = element.attributes
        val size = attributes.length
        if (size == 0) {
            _attribute = mutableMapOf()
            return
        }
        _attribute = LinkedHashMap(size)
        for (i in 0 until size) {
            val attr = attributes.item(i)
            _attribute[attr.nodeName] = attr.nodeValue
        }
    }

    /**
     * 属性値を返す。
     *
     * @param name 属性名
     * @return 属性値、見つからない場合null
     */
    fun getAttribute(name: String?): String? {
        return _attribute[name]
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(value)
        _attribute.forEach {
            sb.append("\n")
            sb.append("@")
            sb.append(it.key)
            sb.append(" => ")
            sb.append(it.value)
        }
        return sb.toString()
    }

    /**
     * Parcelable用のコンストラクタ。
     *
     * @param parcel Parcel
     */
    private constructor(parcel: Parcel) {
        name = parcel.readString()!!
        value = parcel.readString()!!
        val size = parcel.readInt()
        _attribute = LinkedHashMap(size)
        for (i in 0 until size) {
            val name = parcel.readString()!!
            val value = parcel.readString()!!
            _attribute[name] = value
        }
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeString(name)
        dest.writeString(value)
        dest.writeInt(_attribute.size)
        _attribute.forEach {
            dest.writeString(it.key)
            dest.writeString(it.value)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<Tag> {
        override fun createFromParcel(parcel: Parcel): Tag {
            return Tag(parcel)
        }

        override fun newArray(size: Int): Array<Tag?> {
            return arrayOfNulls(size)
        }
    }
}