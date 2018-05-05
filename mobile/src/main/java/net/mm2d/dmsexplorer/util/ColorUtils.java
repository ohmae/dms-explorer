/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * テーマとしての色を決定するメソッドを持つクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ColorUtils {
    private ColorUtils() {
        throw new AssertionError();
    }

    private static final float DARKER_RATIO = 0.7f;

    @ColorInt
    public static int getDarkerColor(@ColorInt final int color) {
        return getDarkerColor(color, DARKER_RATIO);
    }

    @ColorInt
    public static int getDarkerColor(
            @ColorInt final int color,
            final float ratio) {
        final int a = Color.alpha(color);
        final int r = (int) (Color.red(color) * ratio + 0.5f);
        final int g = (int) (Color.green(color) * ratio + 0.5f);
        final int b = (int) (Color.blue(color) * ratio + 0.5f);
        return Color.argb(a, r, g, b);
    }
}
