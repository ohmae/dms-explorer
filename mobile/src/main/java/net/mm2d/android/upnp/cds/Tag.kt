/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import android.os.Parcel
import android.os.Parcelable
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
    private val mAttribute: MutableMap<String, String>

    /**
     * 属性値を格納したMapを返す。
     *
     * @return 属性値を格納したUnmodifiable Map
     */
    val attributes: Map<String, String>
        get() = if (mAttribute.isEmpty()) {
            emptyMap()
        } else Collections.unmodifiableMap(mAttribute)

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
            mAttribute = mutableMapOf()
            return
        }
        mAttribute = LinkedHashMap(size)
        for (i in 0 until size) {
            val attr = attributes.item(i)
            mAttribute[attr.nodeName] = attr.nodeValue
        }
    }

    /**
     * 属性値を返す。
     *
     * @param name 属性名
     * @return 属性値、見つからない場合null
     */
    fun getAttribute(name: String?): String? {
        return mAttribute[name]
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(value)
        for ((key, value1) in mAttribute) {
            sb.append("\n")
            sb.append("@")
            sb.append(key)
            sb.append(" => ")
            sb.append(value1)
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
        if (size == 0) {
            mAttribute = mutableMapOf()
        } else {
            mAttribute = LinkedHashMap(size)
            for (i in 0 until size) {
                val name = parcel.readString()
                val value = parcel.readString()
                mAttribute[name!!] = value!!
            }
        }
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeString(name)
        dest.writeString(value)
        dest.writeInt(mAttribute.size)
        for ((key, value1) in mAttribute) {
            dest.writeString(key)
            dest.writeString(value1)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        /**
         * Parcelableのためのフィールド
         */
        @JvmField
        val CREATOR: Parcelable.Creator<Tag> = object : Parcelable.Creator<Tag> {
            override fun createFromParcel(`in`: Parcel): Tag {
                return Tag(`in`)
            }

            override fun newArray(size: Int): Array<Tag?> {
                return arrayOfNulls(size)
            }
        }
    }
}