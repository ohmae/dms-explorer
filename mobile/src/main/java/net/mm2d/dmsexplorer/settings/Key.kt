/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import kotlin.reflect.KClass

/**
 * SharedPreferences用のKeyの定義。
 *
 *
 * PreferenceActivityからも参照する必要があるためpublicであるが、
 * PreferenceActivityを除いてパッケージ外からアクセスしてはならない。
 *
 *
 * [.name]の値をKeyとして利用する。
 * そのため、定義名を変更する場合は設定の引継ぎ処理が必要。
 * [.ordinal]の値は使用してはならない。
 *
 *
 * 値を読み出すために使用するKeyについては
 * 値の型とデフォルト値をここで定義する。
 * 読み書きの時、定義されていない場合や、誤った型の指定を行うと
 * [IllegalArgumentException]が発生する。
 *
 *
 * 使用しなくなった定義値は削除してはならない。
 * OldKeys以下にまとめ、`@Deprecated`をつけておく。
 * OldKeys以下の値は[Maintainer]以外から利用してはならない。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
enum class Key(
    private val type: KClass<*>? = null,
    private val defaultValue: Any? = null
) {
    // 表示用
    VERSION_NUMBER,
    PLAY_STORE,
    PRIVACY_POLICY,
    COPYRIGHT,
    LICENSE,
    SOURCE_CODE,
    // 設定バージョン
    SETTINGS_VERSION(
        Int::class, -1
    ),
    // アプリバージョン
    APP_VERSION(
        Int::class, -1
    ),
    // PreferenceActivity用
    PLAY_MOVIE_MYSELF(
        Boolean::class, true
    ),
    PLAY_MUSIC_MYSELF(
        Boolean::class, true
    ),
    PLAY_PHOTO_MYSELF(
        Boolean::class, true
    ),

    USE_CUSTOM_TABS(
        Boolean::class, true
    ),
    SHOULD_SHOW_DEVICE_DETAIL_ON_TAP(
        Boolean::class, true
    ),
    SHOULD_SHOW_CONTENT_DETAIL_ON_TAP(
        Boolean::class, true
    ),
    DELETE_FUNCTION_ENABLED(
        Boolean::class, false
    ),

    DARK_THEME(
        Boolean::class, false
    ),

    DO_NOT_SHOW_MOVIE_UI_ON_START(
        Boolean::class, false
    ),
    DO_NOT_SHOW_MOVIE_UI_ON_TOUCH(
        Boolean::class, false
    ),
    DO_NOT_SHOW_TITLE_IN_MOVIE_UI(
        Boolean::class, false
    ),
    IS_MOVIE_UI_BACKGROUND_TRANSPARENT(
        Boolean::class, false
    ),

    DO_NOT_SHOW_PHOTO_UI_ON_START(
        Boolean::class, false
    ),
    DO_NOT_SHOW_PHOTO_UI_ON_TOUCH(
        Boolean::class, false
    ),
    DO_NOT_SHOW_TITLE_IN_PHOTO_UI(
        Boolean::class, false
    ),
    IS_PHOTO_UI_BACKGROUND_TRANSPARENT(
        Boolean::class, false
    ),

    ORIENTATION_COLLECTIVE, // 一括設定用
    ORIENTATION_BROWSE(
        String::class, Orientation.UNSPECIFIED.name
    ),
    ORIENTATION_MOVIE(
        String::class, Orientation.UNSPECIFIED.name
    ),
    ORIENTATION_MUSIC(
        String::class, Orientation.UNSPECIFIED.name
    ),
    ORIENTATION_PHOTO(
        String::class, Orientation.UNSPECIFIED.name
    ),
    ORIENTATION_DMC(
        String::class, Orientation.UNSPECIFIED.name
    ),

    REPEAT_MODE_MOVIE(
        String::class, RepeatMode.PLAY_ONCE.name
    ),
    REPEAT_MODE_MUSIC(
        String::class, RepeatMode.PLAY_ONCE.name
    ),
    REPEAT_INTRODUCED(
        Boolean::class, false
    ),

    LOG_SEND_TIME(
        Long::class, 0L
    ),

    SORT_KEY(
        String::class, SortKey.NONE.name
    ),
    SORT_ORDER_ASCENDING(
        Boolean::class, true
    ),

    // OldKeys
    @Deprecated("removed")
    UPDATE_FETCH_TIME,
    @Deprecated("removed")
    UPDATE_AVAILABLE,
    @Deprecated("removed")
    UPDATE_JSON,
    ;

    init {
        if (defaultValue != null) {
            requireNotNull(type)
            require(type.isInstance(defaultValue))
        } else require(type == null)
    }

    internal val isReadWriteKey: Boolean
        get() = type != null

    internal val isBooleanKey: Boolean
        get() = type == Boolean::class

    internal val isIntKey: Boolean
        get() = type == Int::class

    internal val isLongKey: Boolean
        get() = type == Long::class

    internal val isStringKey: Boolean
        get() = type == String::class

    internal val defaultBoolean: Boolean
        get() = defaultValue as Boolean

    internal val defaultInt: Int
        get() = defaultValue as Int

    internal val defaultLong: Long
        get() = defaultValue as Long

    internal val defaultString: String
        get() = defaultValue as String
}
