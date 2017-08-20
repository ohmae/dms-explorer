/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ActivityLifecycleCallbacksAdapter implements ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(
            final Activity activity,
            final Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(final Activity activity) {
    }

    @Override
    public void onActivityResumed(final Activity activity) {
    }

    @Override
    public void onActivityPaused(final Activity activity) {
    }

    @Override
    public void onActivityStopped(final Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(
            final Activity activity,
            final Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
    }
}
