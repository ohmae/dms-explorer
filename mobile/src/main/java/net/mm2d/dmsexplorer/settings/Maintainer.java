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
     * 設定データフォーマットのバージョン
     *
     * <table>
     * <tr><th>SETTINGS_VERSION</th><th>VersionName</th><th>状態</th></tr>
     * <tr><td>0</td><td>0.6.20-</td><td>サポート終了 v0.7.38</td></tr>
     * <tr><td>1</td><td>0.7.0-</td><td>現在</td></tr>
     * </table>
     *
     * <p>設定フォーマットを変更した場合は、
     * 旧バージョンからのマイグレーション処理を記述し、設定を持ち越せるようにする。
     * マイグレーションを打ち切った場合は、
     * 設定バージョンが現在のものでなければクリアを行い初期設定で起動するようにする。
     */
    private static final int SETTINGS_VERSION = 1;

    /**
     * 起動時に一度だけ呼び出され、SharedPreferencesのメンテナンスを行う。
     *
     * @param storage SettingsStorage
     */
    static void maintain(@NonNull final SettingsStorage storage) {
        final int currentVersion = storage.readInt(Key.SETTINGS_VERSION);
        if (currentVersion == SETTINGS_VERSION) {
            return;
        }
        storage.clear();
        storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION);
        writeDefaultValue(storage, false);
    }

    /**
     * デフォルト値の書き込みを行う
     *
     * @param storage   SettingsStorage
     * @param overwrite true:値を上書きする、false:値がない場合のみ書き込む
     */
    private static void writeDefaultValue(
            @NonNull final SettingsStorage storage,
            final boolean overwrite) {
        for (final Key key : Key.values()) {
            storage.writeDefault(key, overwrite);
        }
    }
}
