/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface ThemeModel {
    void setThemeColor(@NonNull Activity activity, @ColorInt int toolbarColor, @ColorInt int statusBarColor);

    @ColorInt
    int getToolbarColor(@NonNull Activity activity);

    @ColorInt
    int getStatusBarColor(@NonNull Activity activity);
}
