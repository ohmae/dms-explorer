/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.net;

import net.mm2d.util.NetworkUtils;

import java.net.NetworkInterface;
import java.util.Collection;

/**
 * エミュレーター環境でのLAN接続情報を扱うクラス。
 *
 * <p>AVDの環境ではWi-Fi接続は提供されないが、
 * 3G回線をLAN接続として扱うことで動作可能な環境が存在する。
 * 有効なネットワーク接続をLAN接続であるとして返却することで動作させる。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class EmulatorLan extends Lan {
    EmulatorLan() {
    }

    @Override
    public boolean hasAvailableInterface() {
        return true;
    }

    @Override
    public Collection<NetworkInterface> getAvailableInterfaces() {
        return NetworkUtils.getAvailableInet4Interfaces();
    }
}
