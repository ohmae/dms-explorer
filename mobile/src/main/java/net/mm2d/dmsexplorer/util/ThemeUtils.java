/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * テーマとしての色を決定するメソッドを持つクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ThemeUtils {
    private static final float DARKER_RATIO = 0.7f;

    @ColorInt
    public static int getIconColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        final float[] hsv = new float[3];
        hsv[0] = (59 * c) % 360;
        hsv[1] = 111f / 255f;
        hsv[2] = 237f / 255f;
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int getVividColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        final float[] hsv = new float[3];
        hsv[0] = (59 * c) % 360;
        hsv[1] = 185f / 255f;
        hsv[2] = 187f / 255f;
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int getDarkerColor(@ColorInt final int color) {
        final int a = Color.alpha(color);
        final int r = (int) (Color.red(color) * DARKER_RATIO + 0.5f);
        final int g = (int) (Color.green(color) * DARKER_RATIO + 0.5f);
        final int b = (int) (Color.blue(color) * DARKER_RATIO + 0.5f);
        return Color.argb(a, r, g, b);
    }

    @ColorInt
    public static int getPastelColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        final float[] hsv = new float[3];
        hsv[0] = (59 * c) % 360;
        hsv[1] = 124f / 255f;
        hsv[2] = 210f / 255f;
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int getDeepColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        final float[] hsv = new float[3];
        hsv[0] = (59 * c) % 360;
        hsv[1] = 192f / 255f;
        hsv[2] = 96f / 255f;
        return Color.HSVToColor(hsv);
    }
}
