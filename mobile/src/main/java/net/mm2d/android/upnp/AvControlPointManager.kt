/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp

import android.os.Handler
import android.os.Looper
import net.mm2d.android.upnp.avt.MrControlPoint
import net.mm2d.android.upnp.cds.MsControlPoint
import net.mm2d.upnp.Adapter.iconFilter
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.ControlPointFactory
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * UPnP AVのControlPoint機能を管理する。
 *
 * 各DeviceTypeに特化した機能に対しControlPointWrapperに対し、
 * 一つのControlPointインスタンスで対応するため、
 * ControlPointのライフサイクルに関係する処理をこのクラスで管理する
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class AvControlPointManager {
    private val initialized = AtomicBoolean()
    private var controlPoint: ControlPoint? = null
    /**
     * MsControlPointのインスタンスを返す。
     *
     * @return MsControlPoint
     */
    val msControlPoint = MsControlPoint()
    /**
     * MrControlPointのインスタンスを返す。
     *
     * @return MrControlPoint
     */
    val mrControlPoint = MrControlPoint()

    /**
     * 初期化が完了しているか。
     *
     * @return 初期化完了していればtrue
     */
    val isInitialized: Boolean
        get() = initialized.get()

    /**
     * SSDP Searchを実行する。
     *
     * Searchパケットを一つ投げるのみであり、定期的に実行するにはアプリ側での実装が必要。
     */
    fun search() {
        if (!initialized.get()) {
            throw IllegalStateException("ControlPoint is not initialized")
        }
        controlPoint?.search(null)
    }

    fun addPinnedDevice(location: String) {
        if (!initialized.get()) {
            throw IllegalStateException("ControlPoint is not initialized")
        }
        controlPoint?.tryAddPinnedDevice(location)
    }

    /**
     * 初期化する。
     *
     * @param interfaces 使用するインターフェース
     */
    fun initialize(interfaces: Collection<NetworkInterface>?) {
        if (initialized.get()) {
            terminate()
        }
        initialized.set(true)
        val handler = Handler(Looper.getMainLooper())
        controlPoint = ControlPointFactory.builder()
            .setInterfaces(interfaces)
            .setCallbackHandler { r: Runnable -> handler.post(r) }
            .build()
        controlPoint?.setIconFilter(ICON_FILTER)

        msControlPoint.initialize(controlPoint!!)
        mrControlPoint.initialize(controlPoint!!)

        controlPoint?.initialize()
    }

    /**
     * 処理を開始する。
     */
    fun start() {
        if (!initialized.get()) {
            throw IllegalStateException("ControlPoint is not initialized")
        }
        controlPoint?.start()
    }

    /**
     * 処理を終了する。
     */
    fun stop() {
        if (!initialized.get()) {
            return
        }
        controlPoint?.stop()
    }

    /**
     * 終了する。
     */
    fun terminate() {
        if (!initialized.getAndSet(false)) {
            return
        }
        mrControlPoint.terminate(controlPoint!!)
        msControlPoint.terminate(controlPoint!!)

        controlPoint?.terminate()
        controlPoint = null
    }

    companion object {
        private val ICON_FILTER = iconFilter { list ->
            listOf(Collections.max(list, DownloadIcon.COMPARATOR))
        }
    }
}
