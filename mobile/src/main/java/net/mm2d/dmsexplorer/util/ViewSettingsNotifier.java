/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.content.Context;
import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.Const;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewSettingsNotifier extends LocalBroadcastNotifier {
    public ViewSettingsNotifier(@NonNull final Context context) {
        super(context, Const.ACTION_UPDATE_VIEW_SETTINGS);
    }
}
