/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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
        fun get(context: Context): SharedPreferences {
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
        preferences.edit { clear() }
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
     * keyの値を削除する
     *
     * @param key Key
     */
    fun remove(key: Key) {
        preferences.edit { remove(key.name) }
    }

    /**
     * boolean値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeBoolean(key: Key, value: Boolean) {
        require(key.isBooleanKey) { key.name + " is not key for boolean" }
        preferences.edit { putBoolean(key.name, value) }
    }

    /**
     * boolean値を読み出す。
     *
     * @param key Key
     * @return 読み出したboolean値
     */
    fun readBoolean(key: Key): Boolean {
        require(key.isBooleanKey) { key.name + " is not key for boolean" }
        return preferences.getBoolean(key.name, key.defaultBoolean)
    }

    /**
     * int値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeInt(key: Key, value: Int) {
        require(key.isIntKey) { key.name + " is not key for int" }
        preferences.edit { putInt(key.name, value) }
    }

    /**
     * int値を読み出す。
     *
     * @param key Key
     * @return 読み出したint値
     */
    fun readInt(key: Key): Int {
        require(key.isIntKey) { key.name + " is not key for int" }
        return preferences.getInt(key.name, key.defaultInt)
    }

    /**
     * long値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeLong(key: Key, value: Long) {
        require(key.isLongKey) { key.name + " is not key for long" }
        preferences.edit { putLong(key.name, value) }
    }

    /**
     * long値を読み出す。
     *
     * @param key Key
     * @return 読み出したlong値
     */
    fun readLong(key: Key): Long {
        require(key.isLongKey) { key.name + " is not key for long" }
        return preferences.getLong(key.name, key.defaultLong)
    }

    /**
     * String値を書き込む。
     *
     * @param key   Key
     * @param value 書き込む値
     */
    fun writeString(key: Key, value: String) {
        require(key.isStringKey) { key.name + " is not key for String" }
        preferences.edit { putString(key.name, value) }
    }

    /**
     * String値を読み出す。
     *
     * @param key Key
     * @return 読み出したString値
     */
    fun readString(key: Key): String {
        require(key.isStringKey) { key.name + " is not key for String" }
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
