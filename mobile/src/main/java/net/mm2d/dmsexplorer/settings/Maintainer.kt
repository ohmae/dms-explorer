/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import net.mm2d.dmsexplorer.BuildConfig

/**
 * 設定値のメンテナー。
 *
 * アプリ設定のバージョンを付与し、
 * 元に設定値のマイグレーション処理や初期値の書き込みを行う。
 *
 * すでに使用しなくなった設定値にアクセスするため、
 * `@Deprecated`指定をしたOldKeysに唯一アクセスしてもよいクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Maintainer {
    /**
     * 設定データフォーマットのバージョン
     *
     * | value | App Version |  状態              |
     * |-------|-------------|--------------------|
     * |  0    | 0.6.20-     |サポート終了 v0.7.38|
     * |  1    | 0.7.0-      |                    |
     * |  2    | 0.7.56-     |現在                |
     *
     * 設定フォーマットを変更した場合は、
     * 旧バージョンからのマイグレーション処理を記述し、設定を持ち越せるようにする。
     * マイグレーションを打ち切った場合は、
     * 設定バージョンが現在のものでなければクリアを行い初期設定で起動するようにする。
     */
    private const val SETTINGS_VERSION = 2

    /**
     * 起動時に一度だけ呼び出され、SharedPreferencesのメンテナンスを行う。
     *
     * @param storage SettingsStorage
     */
    fun maintain(storage: SettingsStorage) {
        val version = storage.readInt(Key.SETTINGS_VERSION)
        if (version <= 0) {
            storage.clear()
            storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION)
        } else if (version == 1) {
            storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION)
            storage.remove(Key.UPDATE_FETCH_TIME)
            storage.remove(Key.UPDATE_AVAILABLE)
            storage.remove(Key.UPDATE_JSON)
        }
        if (storage.readInt(Key.APP_VERSION) != BuildConfig.VERSION_CODE) {
            storage.writeInt(Key.APP_VERSION, BuildConfig.VERSION_CODE)
            writeDefaultValue(storage, false)
        }
    }

    /**
     * 設定値をすべてデフォルト値にもどす
     *
     * @param storage SettingsStorage
     */
    fun reset(storage: SettingsStorage) {
        writeDefaultValue(storage, true)
        storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION)
        storage.writeInt(Key.APP_VERSION, BuildConfig.VERSION_CODE)
    }

    /**
     * デフォルト値の書き込みを行う
     *
     * @param storage   SettingsStorage
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    private fun writeDefaultValue(
        storage: SettingsStorage,
        overwrite: Boolean
    ) {
        for (key in Key.values()) {
            storage.writeDefault(key, overwrite)
        }
    }
}
