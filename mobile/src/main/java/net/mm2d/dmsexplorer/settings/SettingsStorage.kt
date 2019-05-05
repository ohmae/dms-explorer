/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * SharedPreferencesへのアクセスをカプセル化するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 * @constructor
 * @param context コンテキスト
 */
class SettingsStorage(context: Context) {
    private object PreferencesHolder {
        private var preferences: SharedPreferences? = null

        @Synchronized
        internal fun get(context: Context): SharedPreferences {
            preferences?.let {
                return it
            }
            return PreferenceManager.getDefaultSharedPreferences(context).also {
                preferences = it
            }
        }
    }

    private val preferences: SharedPreferences = PreferencesHolder.get(context)

    /**
     * 書き込まれている内容を消去する。
     */
    fun clear() {
        preferences.edit()
            .clear()
            .apply()
    }

    /**
     * keyの値が書き込まれているかを返す。
     *
     * @param key Key
     * @return 含まれている場合true
     */
    operator fun contains(key: Key): Boolean {
        return preferences.contains(key.name)
    }

    /**
     * boolean値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeBoolean(key: Key, value: Boolean) {
        if (!key.isBooleanKey) {
            throw IllegalArgumentException(key.name + " is not key for boolean")
        }
        preferences.edit()
            .putBoolean(key.name, value)
            .apply()
    }

    /**
     * boolean値を読み出す。
     *
     * @param key Key
     * @return 読み出したboolean値
     */
    fun readBoolean(key: Key): Boolean {
        if (!key.isBooleanKey) {
            throw IllegalArgumentException(key.name + " is not key for boolean")
        }
        return preferences.getBoolean(key.name, key.defaultBoolean)
    }

    /**
     * int値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeInt(key: Key, value: Int) {
        if (!key.isIntKey) {
            throw IllegalArgumentException(key.name + " is not key for int")
        }
        preferences.edit()
            .putInt(key.name, value)
            .apply()
    }

    /**
     * int値を読み出す。
     *
     * @param key Key
     * @return 読み出したint値
     */
    fun readInt(key: Key): Int {
        if (!key.isIntKey) {
            throw IllegalArgumentException(key.name + " is not key for int")
        }
        return preferences.getInt(key.name, key.defaultInt)
    }

    /**
     * long値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeLong(key: Key, value: Long) {
        if (!key.isLongKey) {
            throw IllegalArgumentException(key.name + " is not key for long")
        }
        preferences.edit()
            .putLong(key.name, value)
            .apply()
    }

    /**
     * long値を読み出す。
     *
     * @param key Key
     * @return 読み出したlong値
     */
    fun readLong(key: Key): Long {
        if (!key.isLongKey) {
            throw IllegalArgumentException(key.name + " is not key for long")
        }
        return preferences.getLong(key.name, key.defaultLong)
    }

    /**
     * String値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeString(key: Key, value: String) {
        if (!key.isStringKey) {
            throw IllegalArgumentException(key.name + " is not key for String")
        }
        preferences.edit()
            .putString(key.name, value)
            .apply()
    }

    /**
     * String値を読み出す。
     *
     * @param key Key
     * @return 読み出したString値
     */
    fun readString(key: Key): String {
        if (!key.isStringKey) {
            throw IllegalArgumentException(key.name + " is not key for String")
        }
        return preferences.getString(key.name, key.defaultString)!!
    }

    /**
     * デフォルト値を書き込む。
     *
     * @param key       Key
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    fun writeDefault(key: Key, overwrite: Boolean) {
        if (!key.isReadWriteKey) {
            return
        }
        if (!overwrite && contains(key)) {
            return
        }
        when {
            key.isBooleanKey -> writeBoolean(key, key.defaultBoolean)
            key.isIntKey -> writeInt(key, key.defaultInt)
            key.isLongKey -> writeLong(key, key.defaultLong)
            key.isStringKey -> writeString(key, key.defaultString)
        }
    }
}
