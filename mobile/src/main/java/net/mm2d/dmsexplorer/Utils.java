/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.graphics.Color;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class Utils {
    public static int getAccentColor(String name) {
        final char c = name.isEmpty() ? ' ' : name.charAt(0);
        final float[] hsv = new float[3];
        hsv[0] = (59 * c) % 360;
        hsv[1] = 185f / 255f;
        hsv[2] = 187f / 255f;
        return Color.HSVToColor(hsv);
    }
}
