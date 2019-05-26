/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.app.Activity
import android.graphics.Point
import android.os.Build
import android.view.Display

/**
 * ディスプレイサイズを取得するユーティリティクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object DisplaySizeUtils {
    /**
     * SystemUIを除いたディスプレイサイズを返す。
     *
     * @param activity Activity
     * @return ディスプレイサイズ
     * @see Display.getSize
     */
    @JvmStatic
    fun getSize(activity: Activity): Point {
        val display = activity.windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point
    }

    /**
     * SystemUIを含むディスプレイサイズを返す。
     *
     * @param activity Activity
     * @return ディスプレイサイズ
     * @see Display.getRealSize
     */
    @JvmStatic
    fun getRealSize(activity: Activity): Point {
        return Point().also {
            activity.windowManager.defaultDisplay.getRealSize(it)
        }
    }

    /**
     * アプリケーションエリア内のNavigationBarエリアを返す。
     *
     * @param activity Activity
     * @return NavigationBarのエリア
     */
    @JvmStatic
    fun getNavigationBarArea(activity: Activity): Point {
        if (isInMultiWindowMode(activity)) {
            return Point(0, 0)
        }
        val p1 = getSize(activity)
        val p2 = getRealSize(activity)
        return Point(p2.x - p1.x, p2.y - p1.y)
    }

    private fun isInMultiWindowMode(activity: Activity): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode
    }
}
