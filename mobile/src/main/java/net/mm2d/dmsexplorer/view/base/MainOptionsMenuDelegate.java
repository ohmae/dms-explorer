/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.SettingsActivity;
import net.mm2d.dmsexplorer.view.dialog.UpdateDialog;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class MainOptionsMenuDelegate implements OptionsMenuDelegate {
    @NonNull
    private final BaseActivity mActivity;
    @NonNull
    private final Settings mSettings;
    @NonNull
    private final LocalBroadcastManager mBroadcastManager;
    @NonNull
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(
                final Context context,
                final Intent intent) {
            mActivity.invalidateOptionsMenu();
        }
    };

    MainOptionsMenuDelegate(@NonNull final BaseActivity activity) {
        mActivity = activity;
        mSettings = new Settings(activity);
        mBroadcastManager = LocalBroadcastManager.getInstance(activity);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        mBroadcastManager.registerReceiver(mUpdateReceiver, new IntentFilter(Const.ACTION_UPDATE));
    }

    @Override
    public void onDestroy() {
        mBroadcastManager.unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        mActivity.getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_update).setVisible(mSettings.isUpdateAvailable());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mActivity.navigateUpTo();
                return true;
            case R.id.action_settings:
                SettingsActivity.start(mActivity);
                return true;
            case R.id.action_update:
                UpdateDialog.show(mActivity);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull final Menu menu) {
        menu.findItem(R.id.action_update).setVisible(mSettings.isUpdateAvailable());
        return true;
    }
}
