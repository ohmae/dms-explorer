/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

/**
 * Viewに関連する共通処理をまとめたユーティリティクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ViewUtils {
    /**
     * Viewにサイズが割り当てられた後に実行する。
     *
     * すでに有効な大きさを持っている場合はそのまま実行し、
     * 実行されていなければ[OnGlobalLayoutListener.onGlobalLayout]にて実行する。
     *
     * @param view     対象のView
     * @param runnable 実行する処理
     */
    @JvmStatic
    fun execAfterAllocateSize(view: View, runnable: Runnable) {
        if (view.width == 0 || view.height == 0) {
            execOnLayout(view, runnable)
        } else {
            runnable.run()
        }
    }

    /**
     * Viewのレイアウト後に実行する。
     *
     * 指定されたViewからViewTreeObserverを取得し、
     * [OnGlobalLayoutListener.onGlobalLayout]にて一回のみ実行する。
     *
     * @param view     ViewTreeObserverの取得元View
     * @param runnable 実行する処理
     */
    @JvmStatic
    fun execOnLayout(view: View, runnable: Runnable) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                runnable.run()
            }
        })
    }
}
