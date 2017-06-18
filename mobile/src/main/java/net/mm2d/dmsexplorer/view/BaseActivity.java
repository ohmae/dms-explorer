/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public abstract class BaseActivity extends AppCompatActivity {
    private AtomicBoolean mFinishAfterTransitionLatch = new AtomicBoolean();
    private AtomicBoolean mFinishLatch = new AtomicBoolean();

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (final IllegalStateException ignored) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void finishAfterTransition() {
        if (mFinishAfterTransitionLatch.getAndSet(true)) {
            return;
        }
        super.finishAfterTransition();
    }

    @Override
    public void finish() {
        if (mFinishLatch.getAndSet(true)) {
            return;
        }
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
