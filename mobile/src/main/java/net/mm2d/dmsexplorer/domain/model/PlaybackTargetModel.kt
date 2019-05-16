/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.net.Uri
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.android.upnp.cds.PropertyParser
import net.mm2d.android.upnp.cds.Tag
import net.mm2d.dmsexplorer.domain.entity.ContentEntity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class PlaybackTargetModel(
    val contentEntity: ContentEntity
) {
    private val cdsObject: CdsObject = contentEntity.cdsObject as CdsObject
    private var targetRes: Tag? = cdsObject.getTag(CdsObject.RES)
    var uri: Uri = Uri.EMPTY
        private set
    var mimeType: String? = null
        private set
    val title: String
        get() = cdsObject.title
    val resCount: Int
        get() = contentEntity.resourceCount

    init {
        updateUri()
    }

    fun setResIndex(index: Int) {
        targetRes = cdsObject.getTag(CdsObject.RES, index)
        updateUri()
    }

    private fun updateUri() {
        if (targetRes?.value.isNullOrEmpty()) {
            uri = Uri.EMPTY
            mimeType = null
        } else {
            uri = Uri.parse(targetRes!!.value)
            val protocolInfo = targetRes!!.getAttribute(CdsObject.PROTOCOL_INFO)
            mimeType = PropertyParser.extractMimeTypeFromProtocolInfo(protocolInfo)
        }
    }

    fun createResChoices(): Array<String> {
        val tagList = cdsObject.getTagList(CdsObject.RES) ?: return emptyArray()
        return tagList.map {tag ->
            val protocolInfo = tag.getAttribute(CdsObject.PROTOCOL_INFO)
            val sb = StringBuilder()
            PropertyParser.extractProtocolFromProtocolInfo(protocolInfo)?.let {
                sb.append(it)
            }
            PropertyParser.extractMimeTypeFromProtocolInfo(protocolInfo)?.let {
                if (sb.isNotEmpty()) {
                    sb.append(" ")
                }
                sb.append(it)
            }
            tag.getAttribute(CdsObject.BITRATE)?.let {
                if (sb.isNotEmpty()) {
                    sb.append("\n")
                }
                sb.append("bitrate: ")
                sb.append(it)
            }
            tag.getAttribute(CdsObject.RESOLUTION)?.let {
                if (sb.isNotEmpty()) {
                    sb.append("\n")
                }
                sb.append("resolution: ")
                sb.append(it)
            }
            sb.toString()
        }.toTypedArray()
    }
}
