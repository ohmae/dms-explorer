/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;

import net.mm2d.android.upnp.cds.MediaServer;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ToolbarThemeHelper {
    public static void setServerTheme(
            final @NonNull Activity activity,
            final @NonNull MediaServer server,
            final @NonNull CollapsingToolbarLayout toolbarLayout) {
        setServerTheme(activity, server, toolbarLayout, false);
    }

    public static void setServerTheme(
            final @NonNull Fragment fragment,
            final @NonNull MediaServer server,
            final @NonNull CollapsingToolbarLayout toolbarLayout) {
        setServerTheme(fragment.getActivity(), server, toolbarLayout, true);
    }

    private static void setServerTheme(
            final @NonNull Context context,
            final @NonNull MediaServer server,
            final @NonNull CollapsingToolbarLayout toolbarLayout,
            boolean fragmentTheme) {
        final String friendlyName = server.getFriendlyName();
        int primaryColor = ThemeUtils.getAccentColor(friendlyName);
        int secondaryColor = ThemeUtils.getAccentDarkColor(friendlyName);
        toolbarLayout.setBackgroundColor(primaryColor);
        toolbarLayout.setContentScrimColor(primaryColor);
        if (!fragmentTheme
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && context instanceof Activity) {
            ((Activity) context).getWindow().setStatusBarColor(secondaryColor);
        }
    }
}
