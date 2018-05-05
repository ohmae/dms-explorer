/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PaletteUtils {
    @Nullable
    public static Swatch selectLightSwatch(@NonNull final Palette palette) {
        Swatch swatch;
        swatch = palette.getVibrantSwatch();
        if (swatch != null) {
            return swatch;
        }
        swatch = palette.getMutedSwatch();
        if (swatch != null) {
            return swatch;
        }
        return palette.getDominantSwatch();
    }

    @Nullable
    public static Swatch selectDarkSwatch(@NonNull final Palette palette) {
        Swatch swatch;
        swatch = palette.getDarkVibrantSwatch();
        if (swatch != null) {
            return swatch;
        }
        swatch = palette.getDarkMutedSwatch();
        if (swatch != null) {
            return swatch;
        }
        swatch = palette.getVibrantSwatch();
        if (swatch != null) {
            return swatch;
        }
        swatch = palette.getMutedSwatch();
        if (swatch != null) {
            return swatch;
        }
        return palette.getDominantSwatch();
    }
}
