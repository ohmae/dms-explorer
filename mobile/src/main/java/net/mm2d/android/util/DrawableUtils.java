/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatDrawableManager;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DrawableUtils {
    private DrawableUtils() {
        throw new AssertionError();
    }

    public static Drawable get(
            @NonNull final Context context,
            @DrawableRes int resId) {
        return AppCompatDrawableManager.get().getDrawable(context, resId);
    }
}
