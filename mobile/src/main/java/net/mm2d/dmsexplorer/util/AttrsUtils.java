/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class AttrsUtils {
    private AttrsUtils() {
        throw new AssertionError();
    }

    @ColorInt
    public static int resolveColor(
            @NonNull final Context context,
            @StyleRes final int style,
            @AttrRes final int attr,
            @ColorInt final int defaultColor) {
        final TypedArray a = context.obtainStyledAttributes(style, new int[]{attr});
        final int color = a.getColor(0, defaultColor);
        a.recycle();
        return color;
    }

    @ColorInt
    public static int resolveColor(
            @NonNull final Context context,
            @AttrRes final int attr,
            @ColorInt final int defaultColor) {
        return resolveColor(context, 0, attr, defaultColor);
    }
}
