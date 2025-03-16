/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.os.Build

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object RuntimeEnvironment {
    /**
     * 現在の実行状態がエミュレータか否かを判定する。
     *
     * @return true:エミュレータである。false:それ以外
     */
    val isEmulator: Boolean by lazy {
        Build.UNKNOWN == Build.BOOTLOADER && Build.MODEL.contains("Android SDK")
    }
}
