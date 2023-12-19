/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import android.os.Parcel
import android.os.Parcelable.Creator
import net.mm2d.android.upnp.cds.CdsObject.Companion.TYPE_CONTAINER
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class RootCdsObject(
    override val udn: String,
) : CdsObject {
    override val isItem: Boolean = false
    override val type: Int = TYPE_CONTAINER
    override val objectId: String = "0"
    override val parentId: String = "-1"
    override val upnpClass: String = "object.container"
    override val title: String = ""
    override val rootTag: Tag = Tag.EMPTY
    override fun getValue(xpath: String, index: Int): String? = null
    override fun getValue(tagName: String?, attrName: String?, index: Int): String? = null
    override fun getTag(tagName: String?, index: Int): Tag? = null
    override fun getTagList(tagName: String?): List<Tag>? = null
    override fun getIntValue(xpath: String, defaultValue: Int, index: Int): Int = defaultValue
    override fun getDateValue(xpath: String, index: Int): Date? = null
    override fun getResourceCount(): Int = 0
    override fun hasResource(): Boolean = false
    override fun hasProtectedResource(): Boolean = false
    override fun toDumpString(): String = ""

    override fun describeContents(): Int = 0
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(udn)
    }

    companion object CREATOR : Creator<RootCdsObject> {
        override fun createFromParcel(parcel: Parcel): RootCdsObject =
            RootCdsObject(parcel.readString()!!)

        override fun newArray(size: Int): Array<RootCdsObject?> = arrayOfNulls(size)
    }
}
