/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class Maintainer {
    private static final int SETTINGS_VERSION = 0;

    static void maintain(@NonNull final SharedPreferences pref) {
        if (pref.getInt(Key.SETTINGS_VERSION.name(), -1) == SETTINGS_VERSION) {
            return;
        }
        final Editor editor = pref.edit();
        editor.putInt(Key.SETTINGS_VERSION.name(), SETTINGS_VERSION);
        editor.apply();
    }
}
