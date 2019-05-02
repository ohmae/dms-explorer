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
import java.util.*

/**
 * シンプルなXML情報を表現するクラス。
 *
 * 親要素とその子要素、以外に入れ子関係を持たない
 * シンプルなXML構造を表現する。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class TagMap : Parcelable {
    /**
     * XMLのタグ情報。
     *
     * タグ名をKeyとして、TagのListを保持する。
     * 同一のタグが複数ある場合はListに出現順に格納する。
     */
    private val map: MutableMap<String, MutableList<Tag>>

    val rawMap: Map<String, List<Tag>>
        get() = map

    /**
     * インスタンス作成。
     */
    constructor() {
        map = ArrayMap()
    }

    /**
     * Parcelable用のコンストラクタ。
     *
     * @param parcel Parcel
     */
    private constructor(parcel: Parcel) {
        val size = parcel.readInt()
        map = ArrayMap(size)
        val classLoader = Tag::class.java.classLoader
        for (i in 0 until size) {
            val name = parcel.readString()!!
            val length = parcel.readInt()
            val list = ArrayList<Tag>(length)
            for (j in 0 until length) {
                val tag = parcel.readParcelable<Tag>(classLoader)!!
                list.add(tag)
            }
            map[name] = list
        }
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeInt(map.size)
        for ((key, list) in map) {
            dest.writeString(key)
            dest.writeInt(list.size)
            for (tag in list) {
                dest.writeParcelable(tag, flags)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * タグ情報を格納する。
     *
     * @param name タグ名
     * @param tag  格納するタグ情報
     */
    fun putTag(
        name: String,
        tag: Tag
    ) {
        var tags: MutableList<Tag>? = map[name]
        if (tags == null) {
            tags = ArrayList(1)
            map[name] = tags
        }
        tags.add(tag)
    }

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
    fun getValue(
        xpath: String,
        index: Int = 0
    ): String? {
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
    fun getValue(
        tagName: String?,
        attrName: String?,
        index: Int = 0
    ): String? {
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
    fun getTag(
        tagName: String?,
        index: Int = 0
    ): Tag? {
        val list = getTagList(tagName)
        return if (list == null || list.size <= index) {
            null
        } else list[index]
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
        for ((key, tags) in map) {
            for (i in tags.indices) {
                val tag = tags[i]
                sb.append(key)
                if (tags.size == 1) {
                    sb.append(" => ")
                } else {
                    sb.append("[")
                    sb.append(i.toString())
                    sb.append("] => ")
                }
                sb.append(tag.value)
                sb.append("\n")
                val attrs = tag.attributes
                for ((key1, value) in attrs) {
                    sb.append("      @")
                    sb.append(key1)
                    sb.append(" => ")
                    sb.append(value)
                    sb.append("\n")
                }
            }
        }
        return sb.toString()
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is TagMap) return false
        val obj = o as TagMap?
        return map == obj!!.map
    }

    companion object CREATOR : Creator<TagMap> {
        override fun createFromParcel(parcel: Parcel): TagMap {
            return TagMap(parcel)
        }

        override fun newArray(size: Int): Array<TagMap?> {
            return arrayOfNulls(size)
        }
    }
}
