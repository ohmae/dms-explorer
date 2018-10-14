/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.tabs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CustomTabsHelper extends CustomTabsServiceConnection {
    private static final List<String> PREFERRED_PACKAGES = Arrays.asList(
            "com.android.chrome", // Chrome
            "org.mozilla.firefox", // Firefox
            "com.microsoft.emmx", // Microsoft Edge
            "com.yandex.browser", //Yandex Browser
            "com.sec.android.app.sbrowser", // Samsung Internet Browser
            "com.kiwibrowser.browser", // Kiwi Browser
            "com.brave.browser", // Brave Browser
            "com.chrome.beta",  // Chrome Beta
            "com.chrome.dev",  // Chrome Dev
            "com.chrome.canary", // Chrome Canary
            "org.mozilla.firefox_beta", // Firefox Beta
            "org.mozilla.fennec_aurora", // Firefox Nightly
            "org.mozilla.focus", // Firefox Focus
            "com.yandex.browser.beta", // Yandex Browser (beta)
            "com.yandex.browser.alpha", // Yandex Browser (alpha)
            "com.google.android.apps.chrome" // Chrome Local
    );
    private static final String ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService";
    private static final String EXTRA_CUSTOM_TABS_KEEP_ALIVE =
            "android.support.customtabs.extra.KEEP_ALIVE";
    @Nullable
    private static String sPackageNameToBind;

    public static void addKeepAliveExtra(Context context, Intent intent) {
        Intent keepAliveIntent = new Intent().setClassName(
                context.getPackageName(), KeepAliveService.class.getCanonicalName());
        intent.putExtra(EXTRA_CUSTOM_TABS_KEEP_ALIVE, keepAliveIntent);
    }

    @Nullable
    public static String getPackageNameToBind() {
        return sPackageNameToBind;
    }

    @Nullable
    private static String findPackageNameToUse(@NonNull final Context context) {
        sPackageNameToBind = findPackageNameToUseInner(context);
        return sPackageNameToBind;
    }

    @Nullable
    private static String findPackageNameToUseInner(@NonNull final Context context) {
        final PackageManager pm = context.getPackageManager();
        final Set<String> browsers = OpenUriUtils.getBrowserPackages(context);
        final List<ResolveInfo> services = pm.queryIntentServices(new Intent(ACTION_CUSTOM_TABS_CONNECTION), 0);
        final List<String> candidate = new ArrayList<>();
        for (final ResolveInfo service : services) {
            if (service.serviceInfo == null) {
                continue;
            }
            final String packageName = service.serviceInfo.packageName;
            if (browsers.contains(packageName)) {
                candidate.add(packageName);
            }
        }
        if (candidate.isEmpty()) {
            return null;
        }
        if (candidate.size() == 1) {
            return candidate.get(0);
        }
        final String defaultBrowser = OpenUriUtils.getDefaultBrowserPackage(context);
        if (candidate.contains(defaultBrowser)) {
            return defaultBrowser;
        }
        for (final String packageName : PREFERRED_PACKAGES) {
            if (candidate.contains(packageName)) {
                return packageName;
            }
        }
        return null;
    }

    @NonNull
    private final Context mContext;
    private boolean mBound;
    @Nullable
    private CustomTabsClient mClient;
    @Nullable
    private CustomTabsSession mSession;

    public CustomTabsHelper(@NonNull final Context context) {
        mContext = context;
    }

    public void bind() {
        if (mBound) {
            return;
        }
        final String packageName = findPackageNameToUse(mContext);
        if (packageName == null) {
            return;
        }
        mBound = CustomTabsClient.bindCustomTabsService(mContext, packageName, this);
    }

    void unbind() {
        if (!mBound) {
            return;
        }
        mContext.unbindService(this);
        mBound = false;
        mClient = null;
        mSession = null;
    }

    @Nullable
    public CustomTabsSession getSession() {
        if (mClient == null) {
            mSession = null;
        } else if (mSession == null) {
            mSession = mClient.newSession(new CustomTabsCallback());
        }
        return mSession;
    }

    @Override
    public void onCustomTabsServiceConnected(
            final ComponentName name,
            final CustomTabsClient client) {
        mClient = client;
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mClient = null;
        mSession = null;
    }
}
