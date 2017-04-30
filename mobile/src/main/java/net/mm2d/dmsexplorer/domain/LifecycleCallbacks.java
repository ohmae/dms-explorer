/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import net.mm2d.android.util.ActivityLifecycleCallbacksAdapter;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class LifecycleCallbacks extends ActivityLifecycleCallbacksAdapter {
    @NonNull
    private final Session mSession;
    private int mCreatedCount;
    private boolean mBound;

    LifecycleCallbacks(@NonNull final Session session) {
        mSession = session;
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        mCreatedCount++;
        if (!mBound) {
            mSession.bind();
            mBound = true;
        }
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        mCreatedCount--;
        if (mBound && mCreatedCount == 0 && activity.isFinishing()) {
            mSession.unbind();
            mBound = false;
        }
    }
}
