/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CustomTabsHelper extends CustomTabsServiceConnection {
    private static final String CHROME_STABLE_PACKAGE = "com.android.chrome";
    private static final String ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService";
    private static String sPackageNameToBind;

    @Nullable
    public static String getPackageNameToBind() {
        return sPackageNameToBind;
    }

    @Nullable
    private static String findPackageNameToUse(Context context) {
        sPackageNameToBind = findPackageNameToUseInner(context);
        return sPackageNameToBind;
    }

    @Nullable
    private static String findPackageNameToUseInner(Context context) {
        final PackageManager pm = context.getPackageManager();
        final Intent serviceIntent = new Intent();
        serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
        serviceIntent.setPackage(CHROME_STABLE_PACKAGE);
        if (pm.resolveService(serviceIntent, 0) != null) {
            return CHROME_STABLE_PACKAGE;
        }
        return null;
    }

    @NonNull
    private final Context mContext;
    private boolean mBound;
    private CustomTabsClient mClient;
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

    public void unbind() {
        if (!mBound) {
            return;
        }
        mContext.unbindService(this);
        mBound = false;
        mClient = null;
        mSession = null;
    }

    public CustomTabsSession getSession() {
        if (mClient == null) {
            mSession = null;
        } else if (mSession == null) {
            mSession = mClient.newSession(new CustomTabsCallback());
        }
        return mSession;
    }

    @Override
    public void onCustomTabsServiceConnected(final ComponentName name, final CustomTabsClient client) {
        mClient = client;
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mClient = null;
        mSession = null;
    }
}
