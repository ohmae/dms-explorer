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
    int getVividColor(@NonNull final String title);

    @ColorInt
    int getPastelColor(@NonNull final String title);

    @ColorInt
    int getSlightColor(@NonNull final String title);

    @ColorInt
    int getDeepColor(@NonNull final String title);
}
