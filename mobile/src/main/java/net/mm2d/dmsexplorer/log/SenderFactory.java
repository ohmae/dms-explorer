/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log;

import android.content.Context;
import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.BuildConfig;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class SenderFactory {
    @NonNull
    public static Sender create(@NonNull final Context context) {
        if (BuildConfig.DEBUG) {
            return new DebugSender();
        }
        return new FirebaseSender(context);
    }
}
