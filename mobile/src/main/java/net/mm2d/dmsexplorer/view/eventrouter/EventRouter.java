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
    private static final String ACTION_VIEW_SETTINGS = "ACTION_VIEW_SETTINGS";
    private static final String ACTION_FINISH = "ACTION_FINISH";

    public static EventNotifier createFinishNotifier(@NonNull final Context context) {
        return new LocalBroadcastNotifier(context, ACTION_FINISH);
    }

    public static EventObserver createFinishObserver(@NonNull final Context context) {
        return new LocalBroadcastObserver(context, ACTION_FINISH);
    }

    public static EventNotifier createViewSettingsNotifier(@NonNull final Context context) {
        return new LocalBroadcastNotifier(context, ACTION_VIEW_SETTINGS);
    }

    public static EventObserver createViewSettingsObserver(@NonNull final Context context) {
        return new LocalBroadcastObserver(context, ACTION_VIEW_SETTINGS) {
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
