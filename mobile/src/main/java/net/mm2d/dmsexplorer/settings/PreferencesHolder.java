/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * SharedPreferencesへのアクセスをカプセル化するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class PreferencesHolder {
    private static SharedPreferences sPreferences;

    @NonNull
    private static SharedPreferences getPreferences(@NonNull final Context context) {
        if (sPreferences != null) {
            return sPreferences;
        }
        sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sPreferences;
    }

    /**
     * SharedPreferencesのインスタンスを作成し初期化する。
     *
     * @param context コンテキスト
     */
    static void initialize(@NonNull final Context context) {
        Maintainer.maintain(getPreferences(context));
    }

    @NonNull
    private final SharedPreferences mPreferences;

    /**
     * インスタンス作成。
     *
     * @param context コンテキスト
     */
    PreferencesHolder(@NonNull final Context context) {
        mPreferences = getPreferences(context);
    }

    /**
     * boolean値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    void putBoolean(@NonNull Key key, boolean value) {
        mPreferences.edit()
                .putBoolean(key.name(), value)
                .apply();
    }

    /**
     * boolean値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したboolean値
     */
    boolean getBoolean(@NonNull Key key, boolean defaultValue) {
        return mPreferences.getBoolean(key.name(), defaultValue);
    }

    /**
     * String値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    void putString(@NonNull Key key, @NonNull String value) {
        mPreferences.edit()
                .putString(key.name(), value)
                .apply();
    }

    /**
     * String値を読み出す。
     *
     * @param key          Key
     * @param defaultValue デフォルト値
     * @return 読み出したString値
     */
    String getString(@NonNull Key key, @Nullable String defaultValue) {
        return mPreferences.getString(key.name(), defaultValue);
    }
}
