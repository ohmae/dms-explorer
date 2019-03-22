/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp;

import net.mm2d.upnp.ControlPoint;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 特定のDeviceTypeに特化したControlPointに共通で保持させるインターフェース。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface ControlPointWrapper {
    /**
     * 保持している対応デバイスの個数を返す。
     *
     * @return 対応デバイスの個数
     */
    int getDeviceListSize();

    /**
     * 対応デバイスのリストを返す。
     *
     * @return 対応デバイスのリスト。
     */
    @NonNull
    List<? extends DeviceWrapper> getDeviceList();

    /**
     * 指定UDNに対応した対応デバイスを返す。
     *
     * @param udn UDN
     * @return 対応デバイス、見つからない場合null
     */
    @Nullable
    DeviceWrapper getDevice(@Nullable String udn);

    /**
     * 初期化する。
     *
     * @param controlPoint ControlPoint
     */
    void initialize(@NonNull ControlPoint controlPoint);

    /**
     * 終了する。
     *
     * @param controlPoint ControlPoint
     */
    void terminate(@NonNull ControlPoint controlPoint);
}
