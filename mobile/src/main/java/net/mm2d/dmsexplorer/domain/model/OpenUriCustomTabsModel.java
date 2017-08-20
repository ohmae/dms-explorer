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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;
import android.webkit.URLUtil;

import net.mm2d.android.util.LaunchUtils;
import net.mm2d.dmsexplorer.R;

import java.util.List;

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

    public OpenUriCustomTabsModel(
            @NonNull final CustomTabsHelper helper,
            @NonNull final ThemeModel themeModel) {
        mHelper = helper;
        mThemeModel = themeModel;
    }

    public void setUseCustomTabs(boolean use) {
        mUseCustomTabs = use;
    }

    @Override
    public void openUri(
            @NonNull final Context context,
            @NonNull final String uri) {
        if (!mUseCustomTabs || !URLUtil.isNetworkUrl(uri)
                || hasDefaultAppOtherThanBrowser(context, uri)) {
            LaunchUtils.openUri(context, uri);
            return;
        }
        if (!openUriOnCustomTabs(context, uri)) {
            LaunchUtils.openUri(context, uri);
        }
    }

    private boolean openUriOnCustomTabs(
            @NonNull final Context context,
            @NonNull final String uri) {
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

    private static boolean hasDefaultAppOtherThanBrowser(
            @NonNull final Context context,
            @NonNull final String uri) {
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> browsers = pm.queryIntentActivities(
                makeViewIntent("http://www.example.com/"), 0);
        final Intent intent = makeViewIntent(uri);
        final List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        final ResolveInfo defaultApp = pm.resolveActivity(intent, 0);
        if (defaultApp == null || defaultApp.activityInfo == null) {
            return false;
        }
        return contains(defaultApp, apps) && !contains(defaultApp, browsers);
    }

    private static Intent makeViewIntent(@NonNull final String uri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        return intent;
    }

    private static boolean contains(
            @NonNull final ResolveInfo target,
            @NonNull final List<ResolveInfo> list) {
        final String packageName = target.activityInfo.packageName;
        for (final ResolveInfo info : list) {
            if (TextUtils.equals(packageName, info.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}
