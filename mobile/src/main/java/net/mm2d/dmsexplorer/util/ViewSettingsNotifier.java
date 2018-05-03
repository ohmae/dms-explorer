/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import net.mm2d.dmsexplorer.Const;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewSettingsNotifier {
    @NonNull
    private final LocalBroadcastManager mBroadcastManager;

    public ViewSettingsNotifier(@NonNull final Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void update() {
        mBroadcastManager.sendBroadcast(new Intent(Const.ACTION_UPDATE_VIEW_SETTINGS));
    }
}
