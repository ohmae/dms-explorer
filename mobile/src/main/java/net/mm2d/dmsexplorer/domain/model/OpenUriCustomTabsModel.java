/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;

import net.mm2d.android.util.LaunchUtils;
import net.mm2d.dmsexplorer.R;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class OpenUriCustomTabsModel implements OpenUriModel {
    private static final int DEFAULT_TOOLBAR_COLOR = Color.BLACK;
    @NonNull
    private final CustomTabsHelper mHelper;
    @NonNull
    private final ThemeModel mThemeModel;
    private boolean mUseCustomTabs;

    public OpenUriCustomTabsModel(@NonNull final CustomTabsHelper helper,
                                  @NonNull final ThemeModel themeModel) {
        mHelper = helper;
        mThemeModel = themeModel;
    }

    public void setUseCustomTabs(boolean use) {
        mUseCustomTabs = use;
    }

    @Override
    public void openUri(@NonNull final Context context, @NonNull final String uri) {
        if (!mUseCustomTabs || !openUriOnCustomTabs(context, uri)) {
            LaunchUtils.openUri(context, uri);
        }
    }

    private boolean openUriOnCustomTabs(@NonNull final Context context, @NonNull final String uri) {
        final String packageNameToBind = CustomTabsHelper.getPackageNameToBind();
        if (TextUtils.isEmpty(packageNameToBind)) {
            return false;
        }
        final CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(mHelper.getSession())
                .setShowTitle(true)
                .setToolbarColor(getToolbarColor(context))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                .build();
        customTabsIntent.intent.setPackage(packageNameToBind);
        try {
            customTabsIntent.launchUrl(context, Uri.parse(uri));
        } catch (final ActivityNotFoundException ignored) {
            return false;
        }
        return true;
    }

    private int getToolbarColor(@NonNull final Context context) {
        if (!(context instanceof Activity)) {
            return DEFAULT_TOOLBAR_COLOR;
        }
        final int color = mThemeModel.getToolbarColor((Activity) context);
        return color != 0 ? color : DEFAULT_TOOLBAR_COLOR;
    }
}
