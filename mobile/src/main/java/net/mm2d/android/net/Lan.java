/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.net;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.net.NetworkInterface;
import java.util.Collection;

/**
 * LANの接続情報を取得するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public abstract class Lan {
    /**
     * 環境に応じて適切なインスタンスを作成する。
     *
     * @param context コンテキスト
     * @return インスタンス
     */
    @NonNull
    public static Lan createInstance(Context context) {
        return isEmulator()
                ? new EmulatorLan()
                : new AndroidLan(context);
    }

    /**
     * 現在の実行状態がエミュレータか否かを判定する。
     *
     * @return true:エミュレータである。false:それ以外
     */
    private static boolean isEmulator() {
        return Build.UNKNOWN.equals(Build.BOARD)
                && Build.UNKNOWN.equals(Build.BOOTLOADER);
    }

    /**
     * LANに接続された利用可能なインターフェースが存在するか否かを返す。
     *
     * @return true:インターフェースが存在する。false:それ以外
     */
    public abstract boolean hasAvailableInterface();

    /**
     * LANに接続された利用可能なインターフェースを返す。
     *
     * @return LANに接続された利用可能なインターフェース
     */
    @NonNull
    public abstract Collection<NetworkInterface> getAvailableInterfaces();
}
