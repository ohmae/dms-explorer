/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Settings {
    private static SharedPreferences sPref;

    private static SharedPreferences getPref(@NonNull final Context context) {
        if (sPref != null) {
            return sPref;
        }
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sPref;
    }

    public static void initialize(@NonNull final Context context) {
        Maintainer.maintain(getPref(context));
    }

    private final Context mContext;

    public Settings(@NonNull final Context context) {
        mContext = context;
    }

    private SharedPreferences getPref() {
        return getPref(mContext);
    }

    public boolean isPlayMovieMyself() {
        return getPref().getBoolean(Key.PLAY_MOVIE_MYSELF.name(), true);
    }

    public boolean isPlayMusicMyself() {
        return getPref().getBoolean(Key.PLAY_MUSIC_MYSELF.name(), true);
    }

    public boolean isPlayPhotoMyself() {
        return getPref().getBoolean(Key.PLAY_PHOTO_MYSELF.name(), true);
    }

    public RepeatMode getRepeatModeMovie() {
        final String name = getPref().getString(
                Key.REPEAT_MODE_MOVIE.name(), RepeatMode.PLAY_ONCE.name());
        return RepeatMode.of(name);
    }

    public void setRepeatModeMovie(@NonNull RepeatMode mode) {
        final Editor editor = getPref().edit();
        editor.putString(Key.REPEAT_MODE_MOVIE.name(), mode.name());
        editor.apply();
    }

    public RepeatMode getRepeatModeMusic() {
        final String name = getPref().getString(
                Key.REPEAT_MODE_MUSIC.name(), RepeatMode.PLAY_ONCE.name());
        return RepeatMode.of(name);
    }

    public void setRepeatModeMusic(@NonNull RepeatMode mode) {
        final Editor editor = getPref().edit();
        editor.putString(Key.REPEAT_MODE_MUSIC.name(), mode.name());
        editor.apply();
    }

    public boolean isRepeatIntroduced() {
        return getPref().getBoolean(Key.REPEAT_INTRODUCED.name(), false);
    }

    public void notifyRepeatIntroduced() {
        final Editor editor = getPref().edit();
        editor.putBoolean(Key.REPEAT_INTRODUCED.name(), true);
        editor.apply();
    }
}
