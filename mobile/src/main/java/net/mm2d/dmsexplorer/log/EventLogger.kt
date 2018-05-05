/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log

import android.content.Context
import android.os.Bundle
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.entity.ContentType
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventLogger {
    private var sender: Sender? = null

    @JvmStatic
    fun initialize(context: Context) {
        sender = SenderFactory.create(context)
    }

    @JvmStatic
    fun sendSelectServer() {
        val serverModel = Repository.get().mediaServerModel ?: return
        val server = serverModel.mediaServer
        val bundle = Bundle()
        bundle.putString(Param.ITEM_NAME, server.friendlyName)
        bundle.putString(Param.ITEM_BRAND, server.manufacture)
        sender?.logEvent("select_server", bundle)
    }

    @JvmStatic
    fun sendSelectRenderer() {
        val rendererModel = Repository.get().mediaRendererModel ?: return
        val renderer = rendererModel.mediaRenderer
        val bundle = Bundle()
        bundle.putString(Param.ITEM_NAME, renderer.friendlyName)
        bundle.putString(Param.ITEM_BRAND, renderer.manufacture)
        sender?.logEvent("select_renderer", bundle)
    }

    @JvmStatic
    fun sendSendContent() {
        val targetModel = Repository.get().playbackTargetModel ?: return
        val entity = targetModel.contentEntity
        val cdsObject = entity.`object` as CdsObject
        val bundle = Bundle()
        bundle.putString(Param.ITEM_VARIANT, cdsObject.getValue(CdsObject.RES_PROTOCOL_INFO))
        bundle.putString(Param.CONTENT_TYPE, getTypeString(entity.type))
        bundle.putString(Param.ORIGIN, if (cdsObject.hasProtectedResource()) "dlna-dtcp" else "dlna")
        bundle.putString(Param.DESTINATION, "dmr")
        sender?.logEvent(Event.SELECT_CONTENT, bundle)
    }

    @JvmStatic
    fun sendPlayContent(playMyself: Boolean) {
        val targetModel = Repository.get().playbackTargetModel ?: return
        val entity = targetModel.contentEntity
        val cdsObject = entity.`object` as CdsObject
        val bundle = Bundle()
        bundle.putString(Param.ITEM_VARIANT, cdsObject.getValue(CdsObject.RES_PROTOCOL_INFO))
        bundle.putString(Param.CONTENT_TYPE, getTypeString(entity.type))
        bundle.putString(Param.ORIGIN, "dlna")
        bundle.putString(Param.DESTINATION, if (playMyself) "myself" else "other")
        sender?.logEvent(Event.SELECT_CONTENT, bundle)
    }

    @JvmStatic
    private fun getTypeString(type: ContentType): String {
        return type.name.toLowerCase(Locale.ENGLISH)
    }
}
