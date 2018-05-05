/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class LocalBroadcastObserver {
    public interface Callback {
        void onReceive();
    }

    @NonNull
    private static final Callback EMPTY_CALLBACK = () -> {
    };
    @NonNull
    private Callback mCallback = EMPTY_CALLBACK;
    @NonNull
    private final String mAction;
    @NonNull
    private final LocalBroadcastManager mBroadcastManager;
    @NonNull
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(
                @NonNull final Context context,
                @NonNull final Intent intent) {
            mCallback.onReceive();
        }
    };

    public LocalBroadcastObserver(
            @NonNull final Context context,
            @NonNull final String action) {
        mAction = action;
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void register(@NonNull final Callback callback) {
        mCallback = callback;
        mBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(mAction));
    }

    public void unregister() {
        mBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }
}
