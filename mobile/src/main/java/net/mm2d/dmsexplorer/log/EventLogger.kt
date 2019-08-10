/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.dmsexplorer.Repository
import net.mm2d.dmsexplorer.domain.entity.ContentType
import net.mm2d.dmsexplorer.settings.Settings
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object EventLogger {
    private var sender: Sender? = null
    private val ONE_DAY = TimeUnit.DAYS.toMillis(1)
    private val DATE_POINT = TimeUnit.HOURS.toMillis(4)
    private val DAILY_LOG_DELAY = TimeUnit.SECONDS.toMillis(3)
    private const val EVENT_DAILY = "daily"
    private const val EVENT_SELECT_SERVER = "select_server"
    private const val EVENT_SELECT_RENDERER = "select_renderer"
    private const val VALUE_DLNA = "dlna"
    private const val VALUE_DLNA_DTCP = "dlna-dtcp"
    private const val VALUE_DMR = "dmr"
    private const val VALUE_MYSELF = "myself"
    private const val VALUE_OTHER = "other"

    fun initialize(context: Context) {
        sender = SenderFactory.create(context)
    }

    private fun calculateDateForLog(time: Long): Long =
        (time + TimeZone.getDefault().rawOffset - DATE_POINT) / ONE_DAY

    fun sendDailyLog() {
        Handler(Looper.getMainLooper()).postDelayed({
            sendDailyLogIfNeed()
        }, DAILY_LOG_DELAY)
    }

    private fun sendDailyLogIfNeed() {
        val settings = Settings.get()
        val sendTime = settings.logSendTime
        val current = System.currentTimeMillis()
        if (calculateDateForLog(sendTime) == calculateDateForLog(current)) {
            return
        }
        settings.logSendTime = current
        sender?.logEvent(EVENT_DAILY, settings.dump)
    }

    fun sendSelectServer() {
        val serverModel = Repository.get().mediaServerModel ?: return
        val server = serverModel.mediaServer
        val bundle = Bundle()
        bundle.putString(Param.ITEM_NAME, server.friendlyName)
        bundle.putString(Param.ITEM_BRAND, server.manufacture)
        sender?.logEvent(EVENT_SELECT_SERVER, bundle)
    }

    fun sendSelectRenderer() {
        val rendererModel = Repository.get().mediaRendererModel ?: return
        val renderer = rendererModel.mediaRenderer
        val bundle = Bundle()
        bundle.putString(Param.ITEM_NAME, renderer.friendlyName)
        bundle.putString(Param.ITEM_BRAND, renderer.manufacture)
        sender?.logEvent(EVENT_SELECT_RENDERER, bundle)
    }

    fun sendSendContent() {
        val targetModel = Repository.get().playbackTargetModel ?: return
        val entity = targetModel.contentEntity
        val cdsObject = entity.cdsObject as CdsObject
        val bundle = Bundle()
        bundle.putString(Param.ITEM_VARIANT, cdsObject.getValue(CdsObject.RES_PROTOCOL_INFO))
        bundle.putString(Param.CONTENT_TYPE, getTypeString(entity.type))
        bundle.putString(
            Param.ORIGIN,
            if (cdsObject.hasProtectedResource()) VALUE_DLNA_DTCP else VALUE_DLNA
        )
        bundle.putString(Param.DESTINATION, VALUE_DMR)
        sender?.logEvent(Event.SELECT_CONTENT, bundle)
    }

    fun sendPlayContent(playMyself: Boolean) {
        val targetModel = Repository.get().playbackTargetModel ?: return
        val entity = targetModel.contentEntity
        val cdsObject = entity.cdsObject as CdsObject
        val bundle = Bundle()
        bundle.putString(Param.ITEM_VARIANT, cdsObject.getValue(CdsObject.RES_PROTOCOL_INFO))
        bundle.putString(Param.CONTENT_TYPE, getTypeString(entity.type))
        bundle.putString(Param.ORIGIN, VALUE_DLNA)
        bundle.putString(Param.DESTINATION, if (playMyself) VALUE_MYSELF else VALUE_OTHER)
        sender?.logEvent(Event.SELECT_CONTENT, bundle)
    }

    private fun getTypeString(type: ContentType): String = type.name.toLowerCase(Locale.ENGLISH)
}
