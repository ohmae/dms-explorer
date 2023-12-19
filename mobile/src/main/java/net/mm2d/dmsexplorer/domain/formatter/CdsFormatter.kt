/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.formatter

import android.content.Context
import android.text.format.DateFormat
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.android.upnp.cds.PropertyParser
import net.mm2d.android.upnp.cds.Tag
import net.mm2d.android.util.toDisplayableString
import net.mm2d.dmsexplorer.R
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object CdsFormatter {
    private lateinit var TB: String
    private lateinit var BS: String
    private lateinit var CS: String

    fun initialize(context: Context) {
        TB = context.getString(R.string.network_tb)
        BS = context.getString(R.string.network_bs)
        CS = context.getString(R.string.network_cs)
    }

    fun parseLongDescription(cdsObject: CdsObject): List<Pair<String, String>> =
        convertLongDescription(makeLongDescription(cdsObject))
            .map { it.first.toDisplayableString() to it.second.toDisplayableString() }

    private fun convertLongDescription(tagList: List<Tag>): List<Pair<String, StringBuilder>> {
        val list = ArrayList<Pair<String, StringBuilder>>()
        var pair: Pair<String, StringBuilder>? = null
        for (tag in tagList) {
            val value = tag.value
            if (value.isEmpty()) {
                continue
            }
            val bytes = value.toByteArray()
            val nameSection = String(bytes, 0, minOf(24, bytes.size))
            val name = nameSection.trim { it <= ' ' }
            if (pair == null || pair.first != name) {
                pair = Pair(name, StringBuilder())
                list.add(pair)
            }
            if (value.length > nameSection.length) {
                if (pair.second.isNotEmpty()) {
                    pair.second.append('\n')
                }
                pair.second.append(value.substring(nameSection.length).trim { it <= ' ' })
            }
        }
        return list
    }

    private fun makeLongDescription(cdsObject: CdsObject): List<Tag> {
        val tagList = cdsObject.getTagList(CdsObject.ARIB_LONG_DESCRIPTION) ?: return emptyList()
        val size = tagList.size
        if (size <= 2) {
            return tagList
        }
        if (cdsObject.rootTag.getAttribute("xmlns:av") != "urn:schemas-sony-com:av") {
            return tagList
        }
        val list = ArrayList<Tag>(size)
        list.addAll(tagList.subList(size - 2, size))
        list.addAll(tagList.subList(0, size - 2))
        return list
    }

    fun makeUpnpLongDescription(cdsObject: CdsObject): String? =
        cdsObject.getValue(CdsObject.UPNP_LONG_DESCRIPTION)

    private fun joinTagValue(
        cdsObject: CdsObject,
        tagName: String,
        delimiter: String,
    ): String? = cdsObject.getTagList(tagName)
        ?.joinToString(delimiter) { it.value }
        ?.toDisplayableString()

    private fun joinMembers(
        cdsObject: CdsObject,
        tagName: String,
    ): String? = cdsObject.getTagList(tagName)
        ?.joinToString("\n") {
            it.value + (it.getAttribute("role")?.let { role -> " : $role" } ?: "")
        }

    fun makeChannel(cdsObject: CdsObject): String? {
        val sb = StringBuilder()
        getNetworkString(cdsObject)?.let {
            sb.append(it)
        }
        cdsObject.getValue(CdsObject.UPNP_CHANNEL_NR)?.let {
            if (sb.isEmpty()) {
                sb.append(it)
            } else {
                it.toIntOrNull()?.let { nr ->
                    sb.append("%1$03d".format(nr / 10 % 100))
                }
            }
        }
        cdsObject.getValue(CdsObject.UPNP_CHANNEL_NAME)?.let {
            if (sb.isNotEmpty()) {
                sb.append("   ")
            }
            sb.append(it)
        }
        return if (sb.isEmpty()) null else sb.toString()
    }

    private fun getNetworkString(cdsObject: CdsObject): String? =
        when (cdsObject.getValue(CdsObject.ARIB_OBJECT_TYPE)) {
            "ARIB_TB" -> TB
            "ARIB_BS" -> BS
            "ARIB_CS" -> CS
            else -> null
        }

    fun makeDate(cdsObject: CdsObject): String? {
        val str = cdsObject.getValue(CdsObject.DC_DATE) ?: return null
        val date = PropertyParser.parseDate(str) ?: return null
        return if (str.length <= 10) {
            DateFormat.format("yyyy/MM/dd (E)", date).toString()
        } else {
            DateFormat.format("yyyy/M/d (E) kk:mm:ss", date).toString()
        }
    }

    fun makeSchedule(cdsObject: CdsObject): String? {
        val start = cdsObject.getDateValue(CdsObject.UPNP_SCHEDULED_START_TIME) ?: return null
        val end = cdsObject.getDateValue(CdsObject.UPNP_SCHEDULED_END_TIME) ?: return null
        val startString = DateFormat.format("yyyy/M/d (E) kk:mm", start).toString()
        val endString = if (end.time - start.time > 12 * 3600 * 1000) {
            DateFormat.format("yyyy/M/d (E) kk:mm", end).toString()
        } else {
            DateFormat.format("kk:mm", end).toString()
        }
        return "$startString ～ $endString"
    }

    fun makeScheduleOrDate(cdsObject: CdsObject): String? =
        makeSchedule(cdsObject) ?: makeDate(cdsObject)

    fun makeGenre(cdsObject: CdsObject): String? =
        cdsObject.getValue(CdsObject.UPNP_GENRE)

    fun makeAlbum(cdsObject: CdsObject): String? =
        cdsObject.getValue(CdsObject.UPNP_ALBUM)

    fun makeArtists(cdsObject: CdsObject): String? =
        joinMembers(cdsObject, CdsObject.UPNP_ARTIST)

    fun makeArtistsSimple(cdsObject: CdsObject): String? =
        joinTagValue(cdsObject, CdsObject.UPNP_ARTIST, " ")

    fun makeActors(cdsObject: CdsObject): String? =
        joinMembers(cdsObject, CdsObject.UPNP_ACTOR)

    fun makeAuthors(cdsObject: CdsObject): String? =
        joinMembers(cdsObject, CdsObject.UPNP_AUTHOR)

    fun makeCreator(cdsObject: CdsObject): String? =
        cdsObject.getValue(CdsObject.DC_CREATOR)

    fun makeDescription(cdsObject: CdsObject): String? =
        joinTagValue(cdsObject, CdsObject.DC_DESCRIPTION, "\n")
}
