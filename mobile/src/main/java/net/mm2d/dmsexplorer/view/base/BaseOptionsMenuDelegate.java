/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class BaseOptionsMenuDelegate implements OptionsMenuDelegate {
    private final Activity mActivity;

    BaseOptionsMenuDelegate(@NonNull final Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActivity.onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull final Menu menu) {
        return false;
    }
}
