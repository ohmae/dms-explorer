/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

/**
 * SharedPreferences用のKeyの定義。
 *
 * <p>name()の値をKeyとして利用する。
 * その為定義名を変更する場合は設定の引継ぎ処理が必要。
 * {@link #ordinal()}の値は使用してはならない。
 *
 * <p>使用しなくなったが引き継ぎのために必要な定義値は
 * {@link OldKey}に移動させパッケージ外からはアクセスできなようにする。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public enum Key {
    // 表示用
    VERSION_NUMBER,
    PLAY_STORE,
    COPYRIGHT,
    LICENSE,
    SOURCE_CODE,
    // 設定バージョン
    SETTINGS_VERSION,
    // PreferenceActivity用
    PLAY_MOVIE_MYSELF,
    PLAY_MUSIC_MYSELF,
    PLAY_PHOTO_MYSELF,
    USE_CUSTOM_TABS,

    REPEAT_MODE_MOVIE,
    REPEAT_MODE_MUSIC,
    REPEAT_INTRODUCED,

    UPDATE_FETCH_TIME,
    UPDATE_AVAILABLE,
    UPDATE_JSON,
}
