/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.support.annotation.NonNull;

/**
 * 設定値のメンテナー。
 *
 * <p>アプリ設定のバージョンを付与し、
 * 元に設定値のマイグレーション処理や初期値の書き込みを行う。
 *
 * <p>すでに使用しなくなった設定値にアクセスするため、
 * {@code @Deprecated}指定をしたOldKeysに唯一アクセスしてもよいクラス。
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
     * @param storage SettingsStorage
     */
    static void maintain(@NonNull final SettingsStorage storage) {
        final int currentVersion = getSettingsVersion(storage);
        if (currentVersion == SETTINGS_VERSION) {
            return;
        }
        if (currentVersion == 0) {
            migrateFrom0(storage);
        }
        storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION);
        writeDefaultValue(storage, false);
    }

    /**
     * デフォルト値の書き込みを行う
     *
     * @param storage SettingsStorage
     */
    private static void writeDefaultValue(
            @NonNull final SettingsStorage storage,
            final boolean overwrite) {
        // Playback
        storage.writeBoolean(Key.PLAY_MOVIE_MYSELF, true, overwrite);
        storage.writeBoolean(Key.PLAY_MUSIC_MYSELF, true, overwrite);
        storage.writeBoolean(Key.PLAY_PHOTO_MYSELF, true, overwrite);
        // Function
        storage.writeBoolean(Key.USE_CUSTOM_TABS, true, overwrite);
        storage.writeBoolean(Key.SHOULD_SHOW_DEVICE_DETAIL_ON_TAP, true, overwrite);
        storage.writeBoolean(Key.SHOULD_SHOW_CONTENT_DETAIL_ON_TAP, true, overwrite);
        storage.writeBoolean(Key.DELETE_FUNCTION_ENABLED, false, overwrite);
        // View
        storage.writeBoolean(Key.DARK_THEME, false, overwrite);
        // Expert
        storage.writeBoolean(Key.DO_NOT_SHOW_MOVIE_UI_ON_START, false, overwrite);
        storage.writeBoolean(Key.DO_NOT_SHOW_MOVIE_UI_ON_TOUCH, false, overwrite);
        storage.writeBoolean(Key.DO_NOT_SHOW_TITLE_IN_MOVIE_UI, false, overwrite);
        storage.writeBoolean(Key.IS_MOVIE_UI_BACKGROUND_TRANSPARENT, false, overwrite);
        storage.writeBoolean(Key.DO_NOT_SHOW_PHOTO_UI_ON_START, false, overwrite);
        storage.writeBoolean(Key.DO_NOT_SHOW_PHOTO_UI_ON_TOUCH, false, overwrite);
        storage.writeBoolean(Key.DO_NOT_SHOW_TITLE_IN_PHOTO_UI, false, overwrite);
        storage.writeBoolean(Key.IS_PHOTO_UI_BACKGROUND_TRANSPARENT, false, overwrite);
        storage.writeString(Key.ORIENTATION_BROWSE, "UNSPECIFIED", overwrite);
        storage.writeString(Key.ORIENTATION_MOVIE, "UNSPECIFIED", overwrite);
        storage.writeString(Key.ORIENTATION_MUSIC, "UNSPECIFIED", overwrite);
        storage.writeString(Key.ORIENTATION_PHOTO, "UNSPECIFIED", overwrite);
        storage.writeString(Key.ORIENTATION_DMC, "UNSPECIFIED", overwrite);

        // RepeatMode
        storage.writeString(Key.REPEAT_MODE_MOVIE, RepeatMode.PLAY_ONCE.name(), overwrite);
        storage.writeString(Key.REPEAT_MODE_MUSIC, RepeatMode.PLAY_ONCE.name(), overwrite);
        storage.writeBoolean(Key.REPEAT_INTRODUCED, false, overwrite);
        // Update
        storage.writeLong(Key.UPDATE_FETCH_TIME, 0, overwrite);
        storage.writeBoolean(Key.UPDATE_AVAILABLE, false, overwrite);
        storage.writeString(Key.UPDATE_JSON, "", overwrite);
    }

    /**
     * SharedPreferencesのバージョンを取得する。
     *
     * @param storage SharedPreferences
     * @return バージョン
     */
    @SuppressWarnings("deprecation")
    private static int getSettingsVersion(@NonNull final SettingsStorage storage) {
        if (storage.contains(Key.LAUNCH_APP_MOVIE)) {
            // バージョン番号を割り振る前の設定値が含まれている
            return 0;
        }
        return storage.readInt(Key.SETTINGS_VERSION, -1);
    }

    /**
     * 設定バージョン0からのマイグレーションを行う。
     *
     * @param storage SharedPreferences
     */
    @SuppressWarnings("deprecation")
    private static void migrateFrom0(@NonNull final SettingsStorage storage) {
        final boolean launchMovie = storage.readBoolean(Key.LAUNCH_APP_MOVIE, true);
        final boolean launchMusic = storage.readBoolean(Key.LAUNCH_APP_MUSIC, true);
        final boolean launchPhoto = storage.readBoolean(Key.LAUNCH_APP_PHOTO, true);
        final boolean auto = storage.readBoolean(Key.MUSIC_AUTO_PLAY, false);
        final RepeatMode repeatMode = auto ? RepeatMode.SEQUENTIAL : RepeatMode.PLAY_ONCE;
        storage.clear();
        storage.writeBoolean(Key.PLAY_MOVIE_MYSELF, launchMovie);
        storage.writeBoolean(Key.PLAY_MUSIC_MYSELF, launchMusic);
        storage.writeBoolean(Key.PLAY_PHOTO_MYSELF, launchPhoto);
        storage.writeString(Key.REPEAT_MODE_MUSIC, repeatMode.name());
    }
}
