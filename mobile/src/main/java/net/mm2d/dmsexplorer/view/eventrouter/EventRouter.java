/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Objects;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class EventRouter {
    private static final String ACTION_UPDATE_AVAILABILITY = "ACTION_UPDATE_AVAILABILITY";
    private static final String ACTION_ORIENTATION_SETTINGS = "ACTION_ORIENTATION_SETTINGS";
    private static final String ACTION_FINISH = "ACTION_FINISH";

    private static LocalBroadcastManager sLocalBroadcastManager;

    public static void initialize(@NonNull final Context context) {
        sLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public static EventNotifier createFinishNotifier() {
        return new LocalBroadcastNotifier(
                Objects.requireNonNull(sLocalBroadcastManager), ACTION_FINISH);
    }

    public static EventObserver createFinishObserver() {
        return new LocalBroadcastObserver(
                Objects.requireNonNull(sLocalBroadcastManager), ACTION_FINISH);
    }

    public static EventNotifier createOrientationSettingsNotifier() {
        return new LocalBroadcastNotifier(
                Objects.requireNonNull(sLocalBroadcastManager), ACTION_ORIENTATION_SETTINGS);
    }

    public static EventObserver createOrientationSettingsObserver() {
        return new LocalBroadcastObserver(
                Objects.requireNonNull(sLocalBroadcastManager), ACTION_ORIENTATION_SETTINGS) {
            @Override
            public void register(@NonNull final Callback callback) {
                callback.onReceive();
                super.register(callback);
            }
        };
    }

    public static EventNotifier createUpdateAvailabilityNotifier() {
        return new LocalBroadcastNotifier(
                Objects.requireNonNull(sLocalBroadcastManager), ACTION_UPDATE_AVAILABILITY);
    }

    public static EventObserver createUpdateAvailabilityObserver() {
        return new LocalBroadcastObserver(
                Objects.requireNonNull(sLocalBroadcastManager), ACTION_UPDATE_AVAILABILITY);
    }
}
