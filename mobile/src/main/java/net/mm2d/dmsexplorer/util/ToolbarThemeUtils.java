/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;
import android.support.v7.widget.Toolbar;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ToolbarThemeUtils {
    public static void setServerThemeColor(
            final @NonNull MediaServer server,
            final @Nullable Bitmap icon) {
        if (server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            return;
        }
        setServerThemeColorInner(server, icon);
    }

    public static void setServerThemeColorAsync(
            final @NonNull MediaServer server,
            final @Nullable Bitmap icon) {
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
            final @NonNull MediaServer server,
            final @Nullable Bitmap icon) {
        final Palette palette = icon == null ? null : new Palette.Builder(icon).generate();
        setServerThemeColorFromPalette(server, palette);
    }

    private static void setServerThemeColorFromPalette(
            final @NonNull MediaServer server,
            final @Nullable Palette palette) {
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
    private static Swatch selectLightSwatch(Palette palette) {
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
    private static Swatch selectDarkSwatch(Palette palette) {
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

    public static void setCdsListTheme(
            @NonNull final Activity activity,
            @NonNull final MediaServer server,
            @NonNull final Toolbar toolbar) {
        final int collapsedColor = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK);
        toolbar.setBackgroundColor(collapsedColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ThemeUtils.getDarkerColor(collapsedColor));
        }
    }

    public static void setCdsDetailTheme(
            @NonNull final Context context,
            @NonNull final CdsObject object,
            @NonNull final CollapsingToolbarLayout toolbarLayout,
            boolean activityTheme) {
        final String title = object.getTitle();
        final int toolbarColor = ThemeUtils.getAccentColor(title);
        toolbarLayout.findViewById(R.id.toolbarBackground)
                .setBackgroundColor(ThemeUtils.getPastelColor(title));
        toolbarLayout.setContentScrimColor(toolbarColor);
        if (activityTheme
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && context instanceof Activity) {
            ((Activity) context).getWindow().setStatusBarColor(ThemeUtils.getDarkerColor(toolbarColor));
        }
    }
}
