/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.Const;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ToolbarThemeUtils {
    public static void setServerThemeColor(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon) {
        if (server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            return;
        }
        setServerThemeColorInner(server, icon);
    }

    public static void setServerThemeColorAsync(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon) {
        if (server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            return;
        }
        if (icon == null) {
            setServerThemeColorInner(server, null);
            return;
        }
        new Palette.Builder(icon).generate(palette -> setServerThemeColorFromPalette(server, palette));
    }

    private static void setServerThemeColorInner(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon) {
        final Palette palette = icon == null ? null : new Palette.Builder(icon).generate();
        setServerThemeColorFromPalette(server, palette);
    }

    private static void setServerThemeColorFromPalette(
            @NonNull final MediaServer server,
            @Nullable final Palette palette) {
        final String friendlyName = server.getFriendlyName();
        int expandedColor = ThemeUtils.getPastelColor(friendlyName);
        int collapsedColor = ThemeUtils.getDeepColor(friendlyName);
        if (palette != null) {
            final Swatch lightSwatch = selectLightSwatch(palette);
            if (lightSwatch != null) {
                expandedColor = lightSwatch.getRgb();
            }
            final Swatch darkSwatch = selectDarkSwatch(palette);
            if (darkSwatch != null) {
                collapsedColor = darkSwatch.getRgb();
            }
        }
        server.putIntTag(Const.KEY_TOOLBAR_EXPANDED_COLOR, expandedColor);
        server.putIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, collapsedColor);
        server.putBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, true);
    }

    @Nullable
    private static Swatch selectLightSwatch(@NonNull final Palette palette) {
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
    private static Swatch selectDarkSwatch(@NonNull final Palette palette) {
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
