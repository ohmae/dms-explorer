/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.base;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
interface OptionsMenuDelegate {
    void onCreate(@Nullable final Bundle savedInstanceState);

    void onDestroy();

    boolean onCreateOptionsMenu(@NonNull final Menu menu);

    boolean onOptionsItemSelected(@NonNull final MenuItem item);

    boolean onPrepareOptionsMenu(@NonNull final Menu menu);
}
