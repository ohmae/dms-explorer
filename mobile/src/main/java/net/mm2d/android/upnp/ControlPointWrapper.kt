/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp

import net.mm2d.upnp.ControlPoint

/**
 * 特定のDeviceTypeに特化したControlPointに共通で保持させるインターフェース。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface ControlPointWrapper {
    /**
     * 保持している対応デバイスの個数を返す。
     *
     * @return 対応デバイスの個数
     */
    val deviceListSize: Int

    /**
     * 対応デバイスのリストを返す。
     *
     * @return 対応デバイスのリスト。
     */
    val deviceList: List<DeviceWrapper>

    /**
     * 指定UDNに対応した対応デバイスを返す。
     *
     * @param udn UDN
     * @return 対応デバイス、見つからない場合null
     */
    fun getDevice(
        udn: String?,
    ): DeviceWrapper?

    /**
     * 初期化する。
     *
     * @param controlPoint ControlPoint
     */
    fun initialize(
        controlPoint: ControlPoint,
    )

    /**
     * 終了する。
     *
     * @param controlPoint ControlPoint
     */
    fun terminate(
        controlPoint: ControlPoint,
    )
}
