/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.eventrouter;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class EventRouter {
    private static final String ACTION_UPDATE_AVAILABILITY = "ACTION_UPDATE_AVAILABILITY";
    private static final String ACTION_ORIENTATION_SETTINGS = "ACTION_ORIENTATION_SETTINGS";
    private static final String ACTION_FINISH = "ACTION_FINISH";

    public static EventNotifier createFinishNotifier(@NonNull final Context context) {
        return new LocalBroadcastNotifier(context, ACTION_FINISH);
    }

    public static EventObserver createFinishObserver(@NonNull final Context context) {
        return new LocalBroadcastObserver(context, ACTION_FINISH);
    }

    public static EventNotifier createOrientationSettingsNotifier(@NonNull final Context context) {
        return new LocalBroadcastNotifier(context, ACTION_ORIENTATION_SETTINGS);
    }

    public static EventObserver createOrientationSettingsObserver(@NonNull final Context context) {
        return new LocalBroadcastObserver(context, ACTION_ORIENTATION_SETTINGS) {
            @Override
            public void register(@NonNull final Callback callback) {
                callback.onReceive();
                super.register(callback);
            }
        };
    }

    public static EventNotifier createUpdateAvailabilityNotifier(@NonNull final Context context) {
        return new LocalBroadcastNotifier(context, ACTION_UPDATE_AVAILABILITY);
    }

    public static EventObserver createUpdateAvailabilityObserver(@NonNull final Context context) {
        return new LocalBroadcastObserver(context, ACTION_UPDATE_AVAILABILITY);
    }
}
