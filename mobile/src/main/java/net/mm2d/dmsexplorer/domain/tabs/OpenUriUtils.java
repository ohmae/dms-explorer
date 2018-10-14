/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.tabs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public class OpenUriUtils {
    @Nullable
    private static String sDefaultBrowserPackage;
    @Nullable
    private static Set<String> sBrowserPackages;

    @NonNull
    static Set<String> getBrowserPackages(@NonNull final Context context) {
        return getBrowserPackages(context, false);
    }

    @NonNull
    static Set<String> getBrowserPackages(
            @NonNull final Context context,
            final boolean update) {
        if (!update && sBrowserPackages != null) {
            return sBrowserPackages;
        }
        sBrowserPackages = getBrowserPackagesInner(context);
        return sBrowserPackages;
    }

    @NonNull
    private static Set<String> getBrowserPackagesInner(@NonNull final Context context) {
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(makeBrowserTestIntent(), 0);
        if (activities.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<String> result = new HashSet<>();
        for (final ResolveInfo browser : activities) {
            result.add(browser.activityInfo.packageName);
        }
        return result;
    }


    @Nullable
    static String getDefaultBrowserPackage(@NonNull final Context context) {
        return getDefaultBrowserPackage(context, false);
    }


    @Nullable
    static String getDefaultBrowserPackage(
            @NonNull final Context context,
            final boolean update) {
        if (!update && sDefaultBrowserPackage != null) {
            return sDefaultBrowserPackage;
        }
        sDefaultBrowserPackage = getDefaultBrowserPackageInner(context);
        return sDefaultBrowserPackage;
    }

    @Nullable
    private static String getDefaultBrowserPackageInner(@NonNull final Context context) {
        final PackageManager pm = context.getPackageManager();
        final ResolveInfo browserInfo = pm.resolveActivity(makeBrowserTestIntent(), 0);
        if (browserInfo == null || browserInfo.activityInfo == null) {
            return null;
        }
        final String packageName = browserInfo.activityInfo.packageName;
        if (getBrowserPackages(context).contains(packageName)) {
            return packageName;
        }
        return null;
    }

    @NonNull
    static Intent makeBrowseIntent(@NonNull final String uri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        return intent;
    }

    @NonNull
    private static Intent makeBrowserTestIntent() {
        return makeBrowseIntent("http://www.example.com/");
    }

    public static boolean hasDefaultAppOtherThanBrowser(
            @NonNull final Context context,
            @NonNull final String uri) {
        final PackageManager pm = context.getPackageManager();
        final Intent intent = makeBrowseIntent(uri);
        final ResolveInfo defaultApp = pm.resolveActivity(intent, 0);
        if (defaultApp == null || defaultApp.activityInfo == null) {
            return false;
        }
        final String packageName = defaultApp.activityInfo.packageName;
        if (getBrowserPackages(context).contains(packageName)) {
            return false;
        }
        final List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        for (final ResolveInfo info : apps) {
            if (info.activityInfo == null) {
                continue;
            }
            if (TextUtils.equals(packageName, info.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}
