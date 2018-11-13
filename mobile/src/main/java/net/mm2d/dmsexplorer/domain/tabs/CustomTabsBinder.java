/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.tabs;

import android.app.Activity;
import android.os.Bundle;

import net.mm2d.android.util.ActivityLifecycleCallbacksAdapter;

import androidx.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CustomTabsBinder extends ActivityLifecycleCallbacksAdapter {
    @NonNull
    private final CustomTabsHelper mSession;
    private int mCreatedCount;

    public CustomTabsBinder(@NonNull final CustomTabsHelper session) {
        mSession = session;
    }

    @Override
    public void onActivityCreated(
            final Activity activity,
            final Bundle savedInstanceState) {
        mCreatedCount++;
        mSession.bind();
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        mCreatedCount--;
        if (mCreatedCount == 0 && activity.isFinishing()) {
            mSession.unbind();
        }
    }
}
