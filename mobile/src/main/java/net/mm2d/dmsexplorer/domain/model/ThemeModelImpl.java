/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import net.mm2d.android.util.ActivityLifecycleCallbacksAdapter;
import net.mm2d.dmsexplorer.util.ThemeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ThemeModelImpl extends ActivityLifecycleCallbacksAdapter implements ThemeModel {
    private final Map<String, Integer> mMap = new HashMap<>();

    @Override
    public void setThemeColor(@NonNull final Activity activity,
                              @ColorInt final int toolbarColor,
                              @ColorInt final int statusBarColor) {
        final int color = statusBarColor != 0 ? statusBarColor : ThemeUtils.getDarkerColor(toolbarColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
        mMap.put(activity.getClass().getName(), toolbarColor);
    }

    @Override
    public int getToolbarColor(@NonNull final Activity activity) {
        final Integer color = mMap.get(activity.getClass().getName());
        return color == null ? 0 : color;
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        mMap.remove(activity.getClass().getName());
    }
}
