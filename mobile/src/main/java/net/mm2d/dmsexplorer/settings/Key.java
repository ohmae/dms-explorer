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
 * <p>PreferenceActivityからも参照する必要があるためpublicであるが、
 * PreferenceActivityを除いてパッケージ外からアクセスしてはならない。
 *
 * <p>{@link #name()}の値をKeyとして利用する。
 * そのため、定義名を変更する場合は設定の引継ぎ処理が必要。
 * {@link #ordinal()}の値は使用してはならない。
 *
 * <p>使用しなくなった定義値は削除してはならない。
 * OldKeys以下にまとめ、{@code @Deprecated}をつけておく。
 * OldKeys以下の値は{@link Maintainer}以外から利用してはならない。
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
    SHOULD_SHOW_DEVICE_DETAIL_ON_TAP,
    SHOULD_SHOW_CONTENT_DETAIL_ON_TAP,

    REPEAT_MODE_MOVIE,
    REPEAT_MODE_MUSIC,
    REPEAT_INTRODUCED,

    DELETE_FUNCTION_ENABLED,

    UPDATE_FETCH_TIME,
    UPDATE_AVAILABLE,
    UPDATE_JSON,

    ORIENTATION_BROWSE,
    ORIENTATION_MOVIE,
    ORIENTATION_MUSIC,
    ORIENTATION_PHOTO,
    ORIENTATION_DMC,

    // OldKeys
    @Deprecated
    LAUNCH_APP_MOVIE,
    @Deprecated
    LAUNCH_APP_MUSIC,
    @Deprecated
    LAUNCH_APP_PHOTO,
    @Deprecated
    MUSIC_AUTO_PLAY,
}
