/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.view.Display;

import java.lang.reflect.InvocationTargetException;

/**
 * ディスプレイサイズを取得するユーティリティクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DisplaySizeUtils {
    /**
     * SystemUIを除いたディスプレイサイズを返す。
     *
     * @param activity Activity
     * @return ディスプレイサイズ
     * @see Display#getSize(Point)
     */
    @TargetApi(VERSION_CODES.HONEYCOMB_MR2)
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
    @TargetApi(VERSION_CODES.HONEYCOMB_MR2)
    public static Point getRealSize(@NonNull final Activity activity) {
        final Display display = activity.getWindowManager().getDefaultDisplay();
        final Point point = new Point();
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
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
}
