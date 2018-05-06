/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface ThemeColorGenerator {
    @ColorInt
    int getIconColor(@NonNull final String title);

    @ColorInt
    int getCollapsedToolbarColor(@NonNull final String title);

    @ColorInt
    int getExpandedToolbarColor(@NonNull final String title);

    @ColorInt
    int getSubToolbarColor(@NonNull final String title);

    @ColorInt
    int getControlColor(@NonNull final String title);
}
