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
    // 0: 0.6.20
    // 1: 0.7.0
    private static final int SETTINGS_VERSION = 1;

    static void maintain(@NonNull final SharedPreferences pref) {
        final int currentVersion = getSettingsVersion(pref);
        if (currentVersion == SETTINGS_VERSION) {
            return;
        }
        if (currentVersion == 0) {
            migrateFrom0(pref);
        }
        final Editor editor = pref.edit();
        editor.putInt(Key.SETTINGS_VERSION.name(), SETTINGS_VERSION);
        editor.apply();
    }

    private static int getSettingsVersion(@NonNull final SharedPreferences pref) {
        if (pref.contains(OldKey.LAUNCH_APP_MOVIE.name())) {
            return 0;
        }
        return pref.getInt(Key.SETTINGS_VERSION.name(), -1);
    }

    private static void migrateFrom0(@NonNull final SharedPreferences pref) {
        final boolean launchMovie = pref.getBoolean(OldKey.LAUNCH_APP_MOVIE.name(), true);
        final boolean launchMusic = pref.getBoolean(OldKey.LAUNCH_APP_MUSIC.name(), true);
        final boolean launchPhoto = pref.getBoolean(OldKey.LAUNCH_APP_PHOTO.name(), true);
        final boolean auto = pref.getBoolean(OldKey.MUSIC_AUTO_PLAY.name(), false);
        final Editor editor = pref.edit();
        editor.clear();
        editor.putBoolean(Key.PLAY_MOVIE_MYSELF.name(), launchMovie);
        editor.putBoolean(Key.PLAY_MUSIC_MYSELF.name(), launchMusic);
        editor.putBoolean(Key.PLAY_PHOTO_MYSELF.name(), launchPhoto);
        if (auto) {
            editor.putString(Key.REPEAT_MODE_MUSIC.name(), RepeatMode.SEQUENTIAL.name());
        }
        editor.apply();
    }
}
