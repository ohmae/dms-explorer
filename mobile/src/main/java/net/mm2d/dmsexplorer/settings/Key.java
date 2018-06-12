/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
 * <p>値を読み出すために使用するKeyについては
 * 値の型とデフォルト値をここで定義する。
 * 読み書きの時、定義されていない場合や、誤った型の指定を行うと
 * {@link IllegalArgumentException}が発生する。
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
    SETTINGS_VERSION(
            Integer.class, -1
    ),
    // アプリバージョン
    APP_VERSION(
            Integer.class, -1
    ),
    // PreferenceActivity用
    PLAY_MOVIE_MYSELF(
            Boolean.class, true
    ),
    PLAY_MUSIC_MYSELF(
            Boolean.class, true
    ),
    PLAY_PHOTO_MYSELF(
            Boolean.class, true
    ),

    USE_CUSTOM_TABS(
            Boolean.class, true
    ),
    SHOULD_SHOW_DEVICE_DETAIL_ON_TAP(
            Boolean.class, true
    ),
    SHOULD_SHOW_CONTENT_DETAIL_ON_TAP(
            Boolean.class, true
    ),
    DELETE_FUNCTION_ENABLED(
            Boolean.class, false
    ),

    DARK_THEME(
            Boolean.class, false
    ),

    DO_NOT_SHOW_MOVIE_UI_ON_START(
            Boolean.class, false
    ),
    DO_NOT_SHOW_MOVIE_UI_ON_TOUCH(
            Boolean.class, false
    ),
    DO_NOT_SHOW_TITLE_IN_MOVIE_UI(
            Boolean.class, false
    ),
    IS_MOVIE_UI_BACKGROUND_TRANSPARENT(
            Boolean.class, false
    ),

    DO_NOT_SHOW_PHOTO_UI_ON_START(
            Boolean.class, false
    ),
    DO_NOT_SHOW_PHOTO_UI_ON_TOUCH(
            Boolean.class, false
    ),
    DO_NOT_SHOW_TITLE_IN_PHOTO_UI(
            Boolean.class, false
    ),
    IS_PHOTO_UI_BACKGROUND_TRANSPARENT(
            Boolean.class, false
    ),

    ORIENTATION_COLLECTIVE, // 一括設定用
    ORIENTATION_BROWSE(
            String.class, Orientation.UNSPECIFIED.name()
    ),
    ORIENTATION_MOVIE(
            String.class, Orientation.UNSPECIFIED.name()
    ),
    ORIENTATION_MUSIC(
            String.class, Orientation.UNSPECIFIED.name()
    ),
    ORIENTATION_PHOTO(
            String.class, Orientation.UNSPECIFIED.name()
    ),
    ORIENTATION_DMC(
            String.class, Orientation.UNSPECIFIED.name()
    ),

    REPEAT_MODE_MOVIE(
            String.class, RepeatMode.PLAY_ONCE.name()
    ),
    REPEAT_MODE_MUSIC(
            String.class, RepeatMode.PLAY_ONCE.name()
    ),
    REPEAT_INTRODUCED(
            Boolean.class, false
    ),

    UPDATE_FETCH_TIME(
            Long.class, 0L
    ),
    UPDATE_AVAILABLE(
            Boolean.class, false
    ),
    UPDATE_JSON(
            String.class, ""
    ),

    LOG_SEND_TIME(
            Long.class, 0L
    ),

    // OldKeys
    @Deprecated
    LAUNCH_APP_MOVIE,
    @Deprecated
    LAUNCH_APP_MUSIC,
    @Deprecated
    LAUNCH_APP_PHOTO,
    @Deprecated
    MUSIC_AUTO_PLAY,;

    @Nullable
    private final Class<?> mType;
    @Nullable
    private final Object mDefaultValue;

    /**
     * 値の読み書きに使用しないKeyの初期化
     */
    Key() {
        mType = null;
        mDefaultValue = null;
    }

    /**
     * 値の読み書きに使用するKeyの初期化
     *
     * @param type         値の型
     * @param defaultValue デフォルト値
     * @param <T>          値の型
     */
    <T> Key(
            @NonNull final Class<T> type,
            @NonNull final T defaultValue) {
        mType = type;
        mDefaultValue = defaultValue;
    }

    boolean isReadWriteKey() {
        return mType != null;
    }

    boolean isBooleanKey() {
        return mType == Boolean.class;
    }

    boolean isIntKey() {
        return mType == Integer.class;
    }

    boolean isLongKey() {
        return mType == Long.class;
    }

    boolean isStringKey() {
        return mType == String.class;
    }

    boolean getDefaultBoolean() {
        return (boolean) mDefaultValue;
    }

    int getDefaultInt() {
        return (int) mDefaultValue;
    }

    long getDefaultLong() {
        return (long) mDefaultValue;
    }

    @NonNull
    String getDefaultString() {
        if (mDefaultValue == null) {
            throw new NullPointerException("Default value is not set");
        }
        return (String) mDefaultValue;
    }
}
