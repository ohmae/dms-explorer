/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.upnp.Icon;
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ToolbarThemeHelper {
    public static void setServerDetailTheme(
            final @NonNull Activity activity,
            final @NonNull MediaServer server,
            final @NonNull CollapsingToolbarLayout toolbarLayout) {
        setServerDetailTheme(activity, server, toolbarLayout, true);
    }

    public static void setServerDetailTheme(
            final @NonNull Fragment fragment,
            final @NonNull MediaServer server,
            final @NonNull CollapsingToolbarLayout toolbarLayout) {
        setServerDetailTheme(fragment.getActivity(), server, toolbarLayout, false);
    }

    private static void setServerDetailTheme(
            final @NonNull Context context,
            final @NonNull MediaServer server,
            final @NonNull CollapsingToolbarLayout toolbarLayout,
            boolean activityTheme) {
        final ImageView image = (ImageView) toolbarLayout.findViewById(R.id.toolbar_icon);
        final Bitmap icon = createIconBitmap(server.getIcon());
        if (icon != null) {
            image.setImageBitmap(icon);
        } else {
            final Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, R.drawable.ic_storage);
            DrawableCompat.setTint(drawable, ThemeUtils.getAccentColor(server.getFriendlyName()));
            image.setImageDrawable(drawable);
        }
        if (!server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            setServerThemeColor(server, icon);
        }
        final int expandedColor = server.getIntTag(Const.KEY_TOOLBAR_EXPANDED_COLOR, Color.BLACK);
        final int collapsedColor = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK);
        toolbarLayout.setBackgroundColor(expandedColor);
        toolbarLayout.setContentScrimColor(collapsedColor);
        if (activityTheme
                && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
                && context instanceof Activity) {
            final int statusBarColor = ThemeUtils.getDarkerColor(collapsedColor);
            ((Activity) context).getWindow().setStatusBarColor(statusBarColor);
        }
    }

    @Nullable
    private static Bitmap createIconBitmap(@Nullable Icon icon) {
        if (icon == null) {
            return null;
        }
        final byte[] binary = icon.getBinary();
        return BitmapFactory.decodeByteArray(binary, 0, binary.length);
    }

    private static void setServerThemeColor(
            final @NonNull MediaServer server,
            final @Nullable Bitmap icon) {
        final String friendlyName = server.getFriendlyName();
        int expandedColor = ThemeUtils.getExpandedTitleBarBackground(friendlyName);
        int collapsedColor = ThemeUtils.getCollapsedTitleBarBackground(friendlyName);
        if (icon != null) {
            final Palette palette = new Palette.Builder(icon).generate();
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

    @NonNull
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

    @NonNull
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
            final @NonNull Activity activity,
            final @NonNull MediaServer server,
            final @NonNull Toolbar toolbar) {
        final int collapsedColor = server.getIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, Color.BLACK);
        toolbar.setBackgroundColor(collapsedColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(ThemeUtils.getDarkerColor(collapsedColor));
        }
    }

    public static void setCdsDetailTheme(
            final @NonNull Activity activity,
            final @NonNull CdsObject object,
            final @NonNull CollapsingToolbarLayout toolbarLayout) {
        setCdsDetailTheme(activity, object, toolbarLayout, true);
    }

    public static void setCdsDetailTheme(
            final @NonNull Fragment fragment,
            final @NonNull CdsObject object,
            final @NonNull CollapsingToolbarLayout toolbarLayout) {
        setCdsDetailTheme(fragment.getActivity(), object, toolbarLayout, false);
    }

    private static void setCdsDetailTheme(
            final @NonNull Context context,
            final @NonNull CdsObject object,
            final @NonNull CollapsingToolbarLayout toolbarLayout,
            boolean activityTheme) {
        final String title = object.getTitle();
        final int toolbarColor = ThemeUtils.getAccentColor(title);
        toolbarLayout.setBackgroundColor(ThemeUtils.getExpandedTitleBarBackground(title));
        toolbarLayout.setContentScrimColor(toolbarColor);
        if (activityTheme
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && context instanceof Activity) {
            ((Activity) context).getWindow().setStatusBarColor(ThemeUtils.getDarkerColor(toolbarColor));
        }
    }
}
