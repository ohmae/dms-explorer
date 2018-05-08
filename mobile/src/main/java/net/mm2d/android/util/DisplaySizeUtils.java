/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Display;

import java.lang.reflect.InvocationTargetException;

/**
 * ディスプレイサイズを取得するユーティリティクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DisplaySizeUtils {
    private DisplaySizeUtils() {
        throw new AssertionError();
    }

    /**
     * SystemUIを除いたディスプレイサイズを返す。
     *
     * @param activity Activity
     * @return ディスプレイサイズ
     * @see Display#getSize(Point)
     */
    @NonNull
    public static Point getSize(@NonNull final Activity activity) {
        final Display display = activity.getWindowManager().getDefaultDisplay();
        final Point point = new Point();
        display.getSize(point);
        return point;
    }

    /**
     * SystemUIを含むディスプレイサイズを返す。
     *
     * @param activity Activity
     * @return ディスプレイサイズ
     * @see Display#getRealSize(Point)
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    @NonNull
    public static Point getRealSize(@NonNull final Activity activity) {
        final Display display = activity.getWindowManager().getDefaultDisplay();
        final Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
            return point;
        }
        try {
            final int width = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
            final int height = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            point.set(width, height);
        } catch (final IllegalAccessException ignored) {
        } catch (final InvocationTargetException ignored) {
        } catch (final NoSuchMethodException ignored) {
        }
        return point;
    }

    /**
     * アプリケーションエリア内のNavigationBarエリアを返す。
     *
     * @param activity Activity
     * @return NavigationBarのエリア
     */
    @NonNull
    public static Point getNavigationBarArea(@NonNull final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT // KitKat未満はアプリエリアと重複しない
                || isInMultiWindowMode(activity)) {
            return new Point(0, 0);
        }
        final Point p1 = getSize(activity);
        final Point p2 = getRealSize(activity);
        return new Point(p2.x - p1.x, p2.y - p1.y);
    }

    private static boolean isInMultiWindowMode(@NonNull final Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }
}
