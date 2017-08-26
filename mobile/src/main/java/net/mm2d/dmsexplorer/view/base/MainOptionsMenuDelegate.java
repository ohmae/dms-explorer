/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base;

import android.app.Activity;
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
    private final Activity mActivity;
    private final Settings mSettings;
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(
                final Context context,
                final Intent intent) {
            mActivity.invalidateOptionsMenu();
        }
    };

    MainOptionsMenuDelegate(@NonNull final Activity activity) {
        mActivity = activity;
        mSettings = new Settings();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(mActivity)
                .registerReceiver(mUpdateReceiver, new IntentFilter(Const.ACTION_UPDATE));
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity)
                .unregisterReceiver(mUpdateReceiver);
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
                mActivity.onBackPressed();
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
