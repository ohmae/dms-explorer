/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

import net.mm2d.android.upnp.ControlPointWrapper
import net.mm2d.upnp.Adapter.discoveryListener
import net.mm2d.upnp.Adapter.notifyEventListener
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.Device
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MediaServerのControlPoint機能。
 *
 * ControlPointは継承しておらず、MediaServerとしてのインターフェースのみを提供する。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MsControlPoint : ControlPointWrapper {
    private val discoveryListener = discoveryListener(
        { discoverDevice(it) },
        { lostDevice(it) }
    )

    private val notifyEventListener = notifyEventListener { service, _, variable, value ->
        val udn = service.device.udn
        val server = getDevice(udn)
        if (server == null || service.serviceId != Cds.CDS_SERVICE_ID) {
            return@notifyEventListener
        }
        if (variable == Cds.CONTAINER_UPDATE_IDS) {
            onNotifyContainerUpdateIds(server, value)
        } else if (variable == Cds.SYSTEM_UPDATE_ID) {
            onNotifySystemUpdateId(server, value)
        }
    }

    private val initialized = AtomicBoolean()
    private val mediaServerMap: MutableMap<String, MediaServer> =
        Collections.synchronizedMap(LinkedHashMap())
    private var msDiscoveryListener: MsDiscoveryListener? = null
    private var containerUpdateIdsListener: ContainerUpdateIdsListener? = null
    private var systemUpdateIdListener: SystemUpdateIdListener? = null

    /**
     * 保持しているMediaServerの個数を返す。
     *
     * @return MediaServerの個数
     */
    override val deviceListSize: Int
        get() = mediaServerMap.size

    /**
     * MediaServerのリストを返す。
     *
     * 内部Mapのコピーを返すため使用注意。
     *
     * @return MediaServerのリスト。
     */
    override val deviceList: List<MediaServer>
        get() = synchronized(mediaServerMap) {
            mediaServerMap.values.toList()
        }

    /**
     * 機器発見のイベントを通知するリスナー。
     */
    interface MsDiscoveryListener {
        /**
         * 機器発見時に通知される。
         *
         * @param server 発見したMediaServer
         */
        fun onDiscover(server: MediaServer)

        /**
         * 機器喪失時に通知される。
         *
         * @param server 喪失したMediaServer
         */
        fun onLost(server: MediaServer)
    }

    /**
     * ContainerUpdateIDsのsubscribeイベントを通知するリスナー。
     */
    interface ContainerUpdateIdsListener {
        /**
         * ContainerUpdateIDsが通知されたときにコールされる。
         *
         * @param server イベントを発行したMediaServer
         * @param ids    更新のあったID
         */
        fun onContainerUpdateIds(
            server: MediaServer,
            ids: List<String>
        )
    }

    /**
     * SystemUpdateIDのsubscribeイベントを通知するリスナー。
     */
    interface SystemUpdateIdListener {
        /**
         * SystemUpdateIDが通知されたときにコールされる。
         *
         * @param server イベントを発行したMediaServer
         * @param id     UpdateID
         */
        fun onSystemUpdateId(
            server: MediaServer,
            id: String
        )
    }

    private fun onNotifyContainerUpdateIds(
        server: MediaServer,
        value: String
    ) {
        containerUpdateIdsListener?.let {
            val values = value.split(',')
            if (values.isEmpty() || values.size % 2 != 0) {
                return
            }
            val ids = ArrayList<String>()
            for (i in 0 until values.size step 2) {
                ids.add(values[i])
            }
            it.onContainerUpdateIds(server, ids)
        }
    }

    private fun onNotifySystemUpdateId(
        server: MediaServer,
        value: String
    ) {
        systemUpdateIdListener?.onSystemUpdateId(server, value)
    }

    /**
     * MediaServerのファクトリーメソッド。
     *
     * @param device Device
     * @return MediaServer
     */
    private fun createMediaServer(device: Device): MediaServer {
        return MediaServer(device)
    }

    private fun discoverDevice(device: Device) {
        if (device.deviceType.startsWith(Cds.MS_DEVICE_TYPE)) {
            discoverMsDevice(device)
            return
        }
        for (embeddedDevice in device.deviceList) {
            discoverMsDevice(embeddedDevice)
        }
    }

    private fun discoverMsDevice(device: Device) {
        val server: MediaServer
        try {
            server = createMediaServer(device)
        } catch (ignored: IllegalArgumentException) {
            return
        }

        mediaServerMap[server.udn] = server
        msDiscoveryListener?.onDiscover(server)
    }

    private fun lostDevice(device: Device) {
        val server = mediaServerMap.remove(device.udn) ?: return
        msDiscoveryListener?.onLost(server)
    }

    /**
     * 機器発見の通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    fun setMsDiscoveryListener(listener: MsDiscoveryListener?) {
        msDiscoveryListener = listener
    }

    /**
     * ContainerUpdateIdsの通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    fun setContainerUpdateIdsListener(listener: ContainerUpdateIdsListener?) {
        containerUpdateIdsListener = listener
    }

    /**
     * SystemUpdateIdの通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    fun setSystemUpdateIdListener(listener: SystemUpdateIdListener) {
        systemUpdateIdListener = listener
    }

    /**
     * 指定UDNに対応したMediaServerを返す。
     *
     * @param udn UDN
     * @return MediaServer、見つからない場合null
     */
    override fun getDevice(udn: String?): MediaServer? {
        return mediaServerMap[udn]
    }

    /**
     * 初期化する。
     *
     * @param controlPoint ControlPoint
     */
    override fun initialize(controlPoint: ControlPoint) {
        if (initialized.get()) {
            terminate(controlPoint)
        }
        initialized.set(true)
        mediaServerMap.clear()
        controlPoint.addDiscoveryListener(discoveryListener)
        controlPoint.addNotifyEventListener(notifyEventListener)
    }

    /**
     * 終了する。
     *
     * @param controlPoint ControlPoint
     */
    override fun terminate(controlPoint: ControlPoint) {
        if (!initialized.getAndSet(false)) {
            return
        }
        controlPoint.removeDiscoveryListener(discoveryListener)
        controlPoint.removeNotifyEventListener(notifyEventListener)
        mediaServerMap.clear()
    }
}
