/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.avt

import net.mm2d.android.upnp.ControlPointWrapper
import net.mm2d.upnp.Adapter.discoveryListener
import net.mm2d.upnp.Adapter.notifyEventListener
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.Device
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MediaRenderer用のControlPointインターフェース
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MrControlPoint : ControlPointWrapper {
    private val discoveryListener = discoveryListener(
        { discoverDevice(it) },
        { lostDevice(it) }
    )
    private val notifyEventListener = notifyEventListener { _, _, _, _ -> }
    private val initialized = AtomicBoolean()
    private val mediaRendererMap: MutableMap<String, MediaRenderer> =
        Collections.synchronizedMap(LinkedHashMap())
    private val mrDiscoveryListeners = ArrayList<MrDiscoveryListener>()

    /**
     * 保持しているMediaRendererの個数を返す。
     *
     * @return MediaRendererの個数
     */
    override val deviceListSize: Int
        get() = mediaRendererMap.size

    /**
     * MediaServerのリストを返す。
     *
     * 内部Mapのコピーを返すため使用注意。
     *
     * @return MediaRendererのリスト。
     */
    override val deviceList: List<MediaRenderer>
        get() = synchronized(mediaRendererMap) {
            mediaRendererMap.values.toList()
        }

    /**
     * 機器発見のイベントを通知するリスナー。
     */
    interface MrDiscoveryListener {
        /**
         * 機器発見時に通知される。
         *
         * @param server 発見したMediaRenderer
         */
        fun onDiscover(server: MediaRenderer)

        /**
         * 機器喪失時に通知される。
         *
         * @param server 喪失したMediaRenderer
         */
        fun onLost(server: MediaRenderer)
    }

    /**
     * MediaRendererのファクトリーメソッド。
     *
     * @param device Device
     * @return MediaRenderer
     */
    private fun createMediaRenderer(device: Device): MediaRenderer =
        MediaRenderer(this, device)

    private fun discoverDevice(device: Device) {
        if (device.deviceType.startsWith(Avt.MR_DEVICE_TYPE) &&
            device.findServiceById(Avt.AVT_SERVICE_ID) != null
        ) {
            discoverMrDevice(device)
            return
        }
        device.deviceList.forEach {
            discoverDevice(it)
        }
    }

    private fun discoverMrDevice(device: Device) {
        val renderer: MediaRenderer = try {
            createMediaRenderer(device)
        } catch (ignored: IllegalArgumentException) {
            return
        }

        mediaRendererMap[renderer.udn] = renderer
        for (listener in mrDiscoveryListeners) {
            listener.onDiscover(renderer)
        }
    }

    private fun lostDevice(device: Device) {
        val renderer = mediaRendererMap.remove(device.udn) ?: return
        for (listener in mrDiscoveryListeners) {
            listener.onLost(renderer)
        }
    }

    /**
     * 機器発見の通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    fun addMrDiscoveryListener(listener: MrDiscoveryListener) {
        if (!mrDiscoveryListeners.contains(listener)) {
            mrDiscoveryListeners.add(listener)
        }
    }

    /**
     * 機器発見の通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    fun removeMrDiscoveryListener(listener: MrDiscoveryListener) {
        mrDiscoveryListeners.remove(listener)
    }

    /**
     * 指定UDNに対応したMediaServerを返す。
     *
     * @param udn UDN
     * @return MediaRenderer、見つからない場合null
     */
    override fun getDevice(udn: String?): MediaRenderer? =
        mediaRendererMap[udn]

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
        mediaRendererMap.clear()
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
        mediaRendererMap.clear()
    }
}
