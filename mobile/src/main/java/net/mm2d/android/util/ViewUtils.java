/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * Viewに関連する共通処理をまとめたユーティリティクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewUtils {
    /**
     * Viewにサイズが割り当てられた後に実行する。
     *
     * <p>すでに有効な大きさを持っている場合はそのまま実行し、
     * 実行されていなければ{@link OnGlobalLayoutListener#onGlobalLayout}にて実行する。
     *
     * @param view     対象のView
     * @param runnable 実行する処理
     */
    public static void execAfterAllocateSize(
            @NonNull final View view,
            @NonNull final Runnable runnable) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            execOnLayout(view, runnable);
            return;
        }
        runnable.run();
    }

    /**
     * Viewのレイアウト後に実行する。
     *
     * <p>指定されたViewからViewTreeObserverを取得し、
     * {@link OnGlobalLayoutListener#onGlobalLayout}にて一回のみ実行する。
     *
     * @param view     ViewTreeObserverの取得元View
     * @param runnable 実行する処理
     */
    public static void execOnLayout(
            @NonNull final View view,
            @NonNull final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                runnable.run();
            }
        });
    }
}
