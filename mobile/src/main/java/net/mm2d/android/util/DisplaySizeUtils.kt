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

import java.lang.reflect.InvocationTargetException

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
        val display = activity.windowManager.defaultDisplay
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point)
            return point
        }
        try {
            val width = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
            val height = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
            point.set(width, height)
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InvocationTargetException) {
        } catch (ignored: NoSuchMethodException) {
        }

        return point
    }

    /**
     * アプリケーションエリア内のNavigationBarエリアを返す。
     *
     * @param activity Activity
     * @return NavigationBarのエリア
     */
    @JvmStatic
    fun getNavigationBarArea(activity: Activity): Point {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT // KitKat未満はアプリエリアと重複しない
                || isInMultiWindowMode(activity)) {
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
