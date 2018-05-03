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

import net.mm2d.dmsexplorer.Const;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewSettingsObserver {
    public interface Callback {
        void onUpdateViewSettings();
    }

    @NonNull
    private static final Callback EMPTY_CALLBACK = () -> {
    };
    @NonNull
    private Callback mCallback = EMPTY_CALLBACK;
    @NonNull
    private final LocalBroadcastManager mBroadcastManager;
    @NonNull
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(
                @NonNull final Context context,
                @NonNull final Intent intent) {
            mCallback.onUpdateViewSettings();
        }
    };

    public ViewSettingsObserver(@NonNull final Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void register(@NonNull final Callback callback) {
        callback.onUpdateViewSettings();
        mCallback = callback;
        mBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(Const.ACTION_UPDATE_VIEW_SETTINGS));
    }

    public void unregister() {
        mBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }
}
