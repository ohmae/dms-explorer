/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ThemeColorGeneratorDefault implements ThemeColorGenerator {
    @Override
    public int getIconColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        return Color.HSVToColor(new float[]{
                (59 * c) % 360,
                111f / 255f,
                237f / 255f,
        });
    }

    @Override
    public int getCollapsedToolbarColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        return Color.HSVToColor(new float[]{
                (59 * c) % 360,
                185f / 255f,
                187f / 255f,
        });
    }

    @Override
    public int getExpandedToolbarColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        return Color.HSVToColor(new float[]{
                (59 * c) % 360,
                124f / 255f,
                210f / 255f,
        });
    }

    @Override
    public int getSubToolbarColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        return Color.HSVToColor(new float[]{
                (59 * c) % 360,
                34f / 255f,
                248f / 255f,
        });
    }

    @Override
    public int getControlColor(@NonNull final String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        return Color.HSVToColor(new float[]{
                (59 * c) % 360,
                192f / 255f,
                96f / 255f,
        });
    }
}
