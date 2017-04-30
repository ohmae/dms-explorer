/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain;

import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.TextUtils;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CustomTabsSessionHelper extends CustomTabsServiceConnection
        implements Session {
    @NonNull
    private final Context mContext;
    private String mPackageNameToBind;
    private CustomTabsClient mClient;
    private CustomTabsSession mSession;

    CustomTabsSessionHelper(@NonNull final Context context) {
        mContext = context;
    }

    @Override
    public void bind() {
        if (mClient != null) {
            return;
        }
        if (TextUtils.isEmpty(mPackageNameToBind)) {
            mPackageNameToBind = CustomTabsHelper.getPackageNameToUse(mContext);
            if (mPackageNameToBind == null) {
                return;
            }
        }
        CustomTabsClient.bindCustomTabsService(mContext, mPackageNameToBind, this);
    }

    @Override
    public void unbind() {
        mContext.unbindService(this);
    }

    public String getPackageNameToBind() {
        return mPackageNameToBind;
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
