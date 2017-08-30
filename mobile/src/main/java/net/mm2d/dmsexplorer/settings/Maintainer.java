/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * 設定値のメンテナー。
 *
 * <p>アプリ設定のバージョンを付与し、
 * 元に設定値のマイグレーション処理や初期値の書き込みを行う。
 *
 * <p>すでに使用しなくなった設定値にアクセスするため、
 * {@code @Deprecated}指定をしたOldKeyに唯一アクセスしてもよいクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class Maintainer {
    /**
     * SharedPreferencesのバージョン。
     *
     * <table>
     * <tr><th>SETTINGS_VERSION</th><th>VersionName</th></tr>
     * <tr><td>0</td><td>0.6.20-</td></tr>
     * <tr><td>1</td><td>0.7.0-</td></tr>
     * </table>
     */
    private static final int SETTINGS_VERSION = 1;

    /**
     * 起動時に一度だけ呼び出され、SharedPreferencesのメンテナンスを行う。
     *
     * @param pref SharedPreferences
     */
    static void maintain(@NonNull final SharedPreferences pref) {
        final int currentVersion = getSettingsVersion(pref);
        if (currentVersion == SETTINGS_VERSION) {
            return;
        }
        if (currentVersion == 0) {
            migrateFrom0(pref);
        }
        pref.edit()
                .putInt(Key.SETTINGS_VERSION.name(), SETTINGS_VERSION)
                .apply();
    }

    /**
     * SharedPreferencesのバージョンを取得する。
     *
     * @param pref SharedPreferences
     * @return バージョン
     */
    @SuppressWarnings("deprecation")
    private static int getSettingsVersion(@NonNull final SharedPreferences pref) {
        if (pref.contains(Key.LAUNCH_APP_MOVIE.name())) {
            // バージョン番号を割り振る前の設定値が含まれている
            return 0;
        }
        return pref.getInt(Key.SETTINGS_VERSION.name(), -1);
    }

    /**
     * 設定バージョン0からのマイグレーションを行う。
     *
     * @param pref SharedPreferences
     */
    @SuppressWarnings("deprecation")
    private static void migrateFrom0(@NonNull final SharedPreferences pref) {
        final boolean launchMovie = pref.getBoolean(Key.LAUNCH_APP_MOVIE.name(), true);
        final boolean launchMusic = pref.getBoolean(Key.LAUNCH_APP_MUSIC.name(), true);
        final boolean launchPhoto = pref.getBoolean(Key.LAUNCH_APP_PHOTO.name(), true);
        final boolean auto = pref.getBoolean(Key.MUSIC_AUTO_PLAY.name(), false);
        pref.edit()
                .clear()
                .putBoolean(Key.PLAY_MOVIE_MYSELF.name(), launchMovie)
                .putBoolean(Key.PLAY_MUSIC_MYSELF.name(), launchMusic)
                .putBoolean(Key.PLAY_PHOTO_MYSELF.name(), launchPhoto)
                .putString(Key.REPEAT_MODE_MUSIC.name(),
                        auto ? RepeatMode.SEQUENTIAL.name() : RepeatMode.PLAY_ONCE.name())
                .apply();
    }
}
