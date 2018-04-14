/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class FirebaseSender implements Sender {
    private FirebaseAnalytics mAnalytics;

    FirebaseSender(@NonNull final Context context) {
        mAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void logEvent(
            @NonNull @Size(min = 1L, max = 40L) final String name,
            @Nullable final Bundle params) {
        mAnalytics.logEvent(name, params);
    }
}
