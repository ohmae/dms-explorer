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
import androidx.collection.ArrayMap

/**
 * シンプルなXML情報を表現するクラス。
 *
 * 親要素とその子要素、以外に入れ子関係を持たない
 * シンプルなXML構造を表現する。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class TagMap(
    val map: Map<String, List<Tag>>
) : Parcelable {
    /**
     * XPATH風の指定で示された値を返す。
     *
     * XPATHはitemもしくはcontainerをルートとして指定する。
     * 名前空間はシンボルをそのまま記述する。
     * 例えば'item@id'及び'container@id'はともに'@id'を指定する。
     * 'item/dc:title'であれば'dc:title'を指定する。
     *
     * 複数同一のタグがあった場合は現れた順にインデックスが付けられる。
     * 属性値はタグと同じインデックスに格納される。
     * 例えば、aというタグが複数あるが、
     * 一つ目に現れたタグにはbという属性はなく、
     * 二つ目に現れたタグにbという属性がある状態で、
     * 'a@b'を指定したとすると、インデックス0の値はnullとなる。
     * 二つ目に現れた属性bの値を取り出すには、インデックス1の値を取り出す必要がある。
     *
     * @param xpath パラメータの位置を表現するXPATH風の指定
     * @param index インデックス値
     * @return 指定された値。見つからない場合はnull
     */
    @JvmOverloads
    fun getValue(xpath: String, index: Int = 0): String? {
        val pos = xpath.indexOf('@')
        if (pos < 0) {
            return getValue(xpath, null, index)
        }
        val tagName = xpath.substring(0, pos)
        val attrName = xpath.substring(pos + 1)
        return getValue(tagName, attrName, index)
    }

    /**
     * タグ名と属性名を指定して値を取り出す。
     *
     * 複数同一のタグがあった場合は現れた順にインデックスが付けられる。
     * 属性値はタグと同じインデックスに格納される。
     *
     * @param tagName  タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @param attrName 属性名、タグの値を取得するときはnullを指定する。
     * @param index    インデックス値
     * @return 指定された値。見つからない場合はnull
     */
    @JvmOverloads
    fun getValue(tagName: String?, attrName: String?, index: Int = 0): String? {
        val tag = getTag(tagName, index) ?: return null
        return if (attrName.isNullOrEmpty()) {
            tag.value
        } else tag.getAttribute(attrName)
    }

    /**
     * 指定したタグ名、インデックスのTagインスタンスを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @param index   インデックス値
     * @return Tagインスタンス、見つからない場合はnull
     */
    @JvmOverloads
    fun getTag(tagName: String?, index: Int = 0): Tag? {
        val list = getTagList(tagName) ?: return null
        return if (index < list.size) list[index] else null
    }

    /**
     * 指定したタグ名のTagインスタンスリストを返す。
     *
     * @param tagName タグ名、ルート要素を指定する場合はnullもしくは空文字列
     * @return Tagインスタンスリスト
     */
    fun getTagList(tagName: String?): List<Tag>? {
        return map[tagName ?: ""]
    }

    override fun toString(): String {
        val sb = StringBuilder()
        map.forEach { entry ->
            for (i in entry.value.indices) {
                val tag = entry.value[i]
                sb.append(entry.key)
                if (entry.value.size == 1) {
                    sb.append(" => ")
                } else {
                    sb.append("[$i] => ")
                }
                sb.append(tag.value)
                sb.append("\n")
                tag.attributes.forEach {
                    sb.append("      @${it.key} => ${it.value}\n")
                }
            }
        }
        return sb.toString()
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TagMap) return false
        return map == other.map
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeInt(map.size)
        for ((key, list) in map) {
            dest.writeString(key)
            dest.writeTypedList(list)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Creator<TagMap> {
        override fun createFromParcel(parcel: Parcel): TagMap {
            return create(parcel)
        }

        override fun newArray(size: Int): Array<TagMap?> {
            return arrayOfNulls(size)
        }

        private fun create(parcel: Parcel): TagMap {
            val size = parcel.readInt()
            val map = ArrayMap<String, List<Tag>>(size)
            for (i in 0 until size) {
                val name = parcel.readString()!!
                map[name] = parcel.createTypedArrayList(Tag.CREATOR)!!
            }
            return TagMap(map)
        }
    }
}
