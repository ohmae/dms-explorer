/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.SettingsActivity;
import net.mm2d.dmsexplorer.view.dialog.UpdateDialog;
import net.mm2d.dmsexplorer.view.eventrouter.EventObserver;
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class MainOptionsMenuDelegate implements OptionsMenuDelegate {
    @NonNull
    private final BaseActivity mActivity;
    @NonNull
    private final Settings mSettings;
    @NonNull
    private final EventObserver mUpdateAvailabilityObserver;

    MainOptionsMenuDelegate(@NonNull final BaseActivity activity) {
        mActivity = activity;
        mSettings = new Settings(activity);
        mUpdateAvailabilityObserver = EventRouter.getUpdateAvailabilityObserver(activity);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        mUpdateAvailabilityObserver.register(mActivity::invalidateOptionsMenu);
    }

    @Override
    public void onDestroy() {
        mUpdateAvailabilityObserver.unregister();
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
