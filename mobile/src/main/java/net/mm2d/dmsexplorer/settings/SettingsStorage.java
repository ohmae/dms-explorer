/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

/**
 * SharedPreferencesへのアクセスをカプセル化するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class SettingsStorage {
    private static class PreferencesHolder {
        private static SharedPreferences sPreferences;

        @NonNull
        static synchronized SharedPreferences get(@NonNull final Context context) {
            if (sPreferences != null) {
                return sPreferences;
            }
            sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sPreferences;
        }
    }

    @NonNull
    private final SharedPreferences mPreferences;

    /**
     * インスタンス作成。
     *
     * @param context コンテキスト
     */
    SettingsStorage(@NonNull final Context context) {
        mPreferences = PreferencesHolder.get(context);
    }

    /**
     * SharedPreferencesのインスタンスを返す。
     *
     * <p>Maintainerで必要な場合のみ利用する。
     * それ以外では使用しないこと。
     *
     * @return SharedPreferences
     */
    @NonNull
    @Deprecated
    SharedPreferences getPreferences() {
        return mPreferences;
    }

    /**
     * 書き込まれている内容を消去する。
     */
    void clear() {
        mPreferences.edit()
                .clear()
                .apply();
    }

    /**
     * keyの値が書き込まれているかを返す。
     *
     * @param key Key
     * @return 含まれている場合true
     */
    boolean contains(@NonNull final Key key) {
        return mPreferences.contains(key.name());
    }

    /**
     * boolean値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    void writeBoolean(
            @NonNull final Key key,
            final boolean value) {
        if (!key.isBooleanKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for boolean");
        }
        mPreferences.edit()
                .putBoolean(key.name(), value)
                .apply();
    }

    /**
     * boolean値を読み出す。
     *
     * @param key Key
     * @return 読み出したboolean値
     */
    boolean readBoolean(@NonNull final Key key) {
        if (!key.isBooleanKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for boolean");
        }
        return mPreferences.getBoolean(key.name(), key.getDefaultBoolean());
    }

    /**
     * int値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    void writeInt(
            @NonNull final Key key,
            final int value) {
        if (!key.isIntKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for int");
        }
        mPreferences.edit()
                .putInt(key.name(), value)
                .apply();
    }

    /**
     * int値を読み出す。
     *
     * @param key Key
     * @return 読み出したint値
     */
    int readInt(@NonNull final Key key) {
        if (!key.isIntKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for int");
        }
        return mPreferences.getInt(key.name(), key.getDefaultInt());
    }

    /**
     * long値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    void writeLong(
            @NonNull final Key key,
            final long value) {
        if (!key.isLongKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for long");
        }
        mPreferences.edit()
                .putLong(key.name(), value)
                .apply();
    }

    /**
     * long値を読み出す。
     *
     * @param key Key
     * @return 読み出したlong値
     */
    long readLong(@NonNull final Key key) {
        if (!key.isLongKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for long");
        }
        return mPreferences.getLong(key.name(), key.getDefaultLong());
    }

    /**
     * String値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    void writeString(
            @NonNull final Key key,
            @NonNull final String value) {
        if (!key.isStringKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for String");
        }
        mPreferences.edit()
                .putString(key.name(), value)
                .apply();
    }

    /**
     * String値を読み出す。
     *
     * @param key Key
     * @return 読み出したString値
     */
    @NonNull
    String readString(@NonNull final Key key) {
        if (!key.isStringKey()) {
            throw new IllegalArgumentException(key.name() + " is not key for String");
        }
        return mPreferences.getString(key.name(), key.getDefaultString());
    }

    /**
     * デフォルト値を書き込む。
     *
     * @param key       Key
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    void writeDefault(
            @NonNull final Key key,
            final boolean overwrite) {
        if (!key.isReadWriteKey()) {
            return;
        }
        if (!overwrite && contains(key)) {
            return;
        }
        if (key.isBooleanKey()) {
            writeBoolean(key, key.getDefaultBoolean());
        } else if (key.isIntKey()) {
            writeInt(key, key.getDefaultInt());
        } else if (key.isLongKey()) {
            writeLong(key, key.getDefaultLong());
        } else if (key.isStringKey()) {
            writeString(key, key.getDefaultString());
        }
    }
}
