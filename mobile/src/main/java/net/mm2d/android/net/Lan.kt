/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.net

import android.content.Context
import net.mm2d.android.util.RuntimeEnvironment
import java.net.NetworkInterface

/**
 * LANの接続情報を取得するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class Lan {
    /**
     * LANに接続された利用可能なインターフェースを返す。
     *
     * @return LANに接続された利用可能なインターフェース
     */
    abstract fun getAvailableInterfaces(): Collection<NetworkInterface>

    /**
     * LANに接続された利用可能なインターフェースが存在するか否かを返す。
     *
     * @return true:インターフェースが存在する。false:それ以外
     */
    abstract fun hasAvailableInterface(): Boolean

    companion object {
        /**
         * 環境に応じて適切なインスタンスを作成する。
         *
         * @param context コンテキスト
         * @return インスタンス
         */
        fun createInstance(context: Context): Lan =
            if (RuntimeEnvironment.isEmulator)
                EmulatorLan()
            else
                AndroidLan(context)
    }
}
