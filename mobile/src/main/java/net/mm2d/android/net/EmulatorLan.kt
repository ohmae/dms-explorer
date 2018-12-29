/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.net

import net.mm2d.util.NetworkUtils
import java.net.NetworkInterface

/**
 * エミュレーター環境でのLAN接続情報を扱うクラス。
 *
 * AVDの環境ではWi-Fi接続は提供されないが、
 * 3G回線をLAN接続として扱うことで動作可能な環境が存在する。
 * 有効なネットワーク接続をLAN接続であるとして返却することで動作させる。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class EmulatorLan : Lan() {
    override fun getAvailableInterfaces(): Collection<NetworkInterface> =
        NetworkUtils.getAvailableInet4Interfaces()

    override fun hasAvailableInterface(): Boolean {
        return true
    }
}
