/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class LocalBroadcastNotifier implements EventNotifier {
    @NonNull
    private final String mAction;
    @NonNull
    private final LocalBroadcastManager mBroadcastManager;

    LocalBroadcastNotifier(
            @NonNull final Context context,
            @NonNull final String action) {
        mAction = action;
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public void send() {
        mBroadcastManager.sendBroadcast(new Intent(mAction));
    }
}
