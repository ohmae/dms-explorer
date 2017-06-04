/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * SharedPreferencesに覚えさせる設定値を集中管理するクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Settings {
    /**
     * アプリ起動時に一度だけコールされ、初期化を行う。
     *
     * @param context コンテキスト
     */
    public static void initialize(@NonNull final Context context) {
        SettingsStorage.initialize(context);
    }

    @NonNull
    private final SettingsStorage mStorage;

    /**
     * インスタンス作成。
     *
     * @param context コンテキスト
     */
    public Settings(@NonNull final Context context) {
        mStorage = new SettingsStorage(context);
    }

    /**
     * 動画再生をアプリで行うか否かを返す。
     *
     * @return アプリで行う場合true
     */
    public boolean isPlayMovieMyself() {
        return mStorage.getBoolean(Key.PLAY_MOVIE_MYSELF, true);
    }

    /**
     * 音楽再生をアプリで行うか否かを返す。
     *
     * @return アプリで行う場合true
     */
    public boolean isPlayMusicMyself() {
        return mStorage.getBoolean(Key.PLAY_MUSIC_MYSELF, true);
    }

    /**
     * 静止画再生をアプリで行うか否かを返す。
     *
     * @return アプリで行う場合true
     */
    public boolean isPlayPhotoMyself() {
        return mStorage.getBoolean(Key.PLAY_PHOTO_MYSELF, true);
    }

    /**
     * 動画再生のリピートモードを設定する。
     *
     * @return 動画再生のリピートモード
     */
    @NonNull
    public RepeatMode getRepeatModeMovie() {
        return RepeatMode.of(mStorage.getString(Key.REPEAT_MODE_MOVIE, ""));
    }

    /**
     * 動画再生のリピートモードを返す。
     *
     * @param mode 動画再生のリピートモード
     */
    public void setRepeatModeMovie(@NonNull RepeatMode mode) {
        mStorage.putString(Key.REPEAT_MODE_MOVIE, mode.name());
    }

    /**
     * 音楽再生のリピートモードを返す。
     *
     * @return 音楽再生のリピートモード
     */
    @NonNull
    public RepeatMode getRepeatModeMusic() {
        return RepeatMode.of(mStorage.getString(Key.REPEAT_MODE_MUSIC, ""));
    }

    /**
     * 音楽再生のリピートモードを設定する。
     *
     * @param mode 音楽再生のリピートモード
     */
    public void setRepeatModeMusic(@NonNull RepeatMode mode) {
        mStorage.putString(Key.REPEAT_MODE_MUSIC, mode.name());
    }

    /**
     * リピートモードの操作案内を表示したか否か。
     *
     * @return 表示した場合true
     */
    public boolean isRepeatIntroduced() {
        return mStorage.getBoolean(Key.REPEAT_INTRODUCED, false);
    }

    /**
     * リピートモードの操作案内を表示した。
     */
    public void notifyRepeatIntroduced() {
        mStorage.putBoolean(Key.REPEAT_INTRODUCED, true);
    }

    /**
     * Chrome Custom Tabsを使用するか否か。
     *
     * @return 使用する場合true
     */
    public boolean useCustomTabs() {
        return mStorage.getBoolean(Key.USE_CUSTOM_TABS, true);
    }
}
