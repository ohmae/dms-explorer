/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.mm2d.dmsexplorer.domain.entity.ContentType;

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
     * 指定コンテンツ種別をアプリで再生するか否かを返す。
     *
     * @param type コンテンツ種別
     * @return アプリで再生する場合true
     */
    public boolean isPlayMyself(@NonNull final ContentType type) {
        switch (type) {
            case MOVIE:
                return isPlayMovieMyself();
            case MUSIC:
                return isPlayMusicMyself();
            case PHOTO:
                return isPlayPhotoMyself();
        }
        return false;
    }

    /**
     * 動画再生をアプリで行うか否かを返す。
     *
     * @return アプリで行う場合true
     */
    public boolean isPlayMovieMyself() {
        return mStorage.readBoolean(Key.PLAY_MOVIE_MYSELF, true);
    }

    /**
     * 音楽再生をアプリで行うか否かを返す。
     *
     * @return アプリで行う場合true
     */
    public boolean isPlayMusicMyself() {
        return mStorage.readBoolean(Key.PLAY_MUSIC_MYSELF, true);
    }

    /**
     * 静止画再生をアプリで行うか否かを返す。
     *
     * @return アプリで行う場合true
     */
    public boolean isPlayPhotoMyself() {
        return mStorage.readBoolean(Key.PLAY_PHOTO_MYSELF, true);
    }

    /**
     * 動画再生のリピートモードを設定する。
     *
     * @return 動画再生のリピートモード
     */
    @NonNull
    public RepeatMode getRepeatModeMovie() {
        return RepeatMode.of(mStorage.readString(Key.REPEAT_MODE_MOVIE, ""));
    }

    /**
     * 動画再生のリピートモードを返す。
     *
     * @param mode 動画再生のリピートモード
     */
    public void setRepeatModeMovie(@NonNull RepeatMode mode) {
        mStorage.writeString(Key.REPEAT_MODE_MOVIE, mode.name());
    }

    /**
     * 音楽再生のリピートモードを返す。
     *
     * @return 音楽再生のリピートモード
     */
    @NonNull
    public RepeatMode getRepeatModeMusic() {
        return RepeatMode.of(mStorage.readString(Key.REPEAT_MODE_MUSIC, ""));
    }

    /**
     * 音楽再生のリピートモードを設定する。
     *
     * @param mode 音楽再生のリピートモード
     */
    public void setRepeatModeMusic(@NonNull RepeatMode mode) {
        mStorage.writeString(Key.REPEAT_MODE_MUSIC, mode.name());
    }

    /**
     * リピートモードの操作案内を表示したか否か。
     *
     * @return 表示した場合true
     */
    public boolean isRepeatIntroduced() {
        return mStorage.readBoolean(Key.REPEAT_INTRODUCED, false);
    }

    /**
     * リピートモードの操作案内を表示した。
     */
    public void notifyRepeatIntroduced() {
        mStorage.writeBoolean(Key.REPEAT_INTRODUCED, true);
    }

    /**
     * Chrome Custom Tabsを使用するか否か。
     *
     * @return 使用する場合true
     */
    public boolean useCustomTabs() {
        return mStorage.readBoolean(Key.USE_CUSTOM_TABS, true);
    }

    /**
     * デバイスリストのシングルタップで詳細を表示するか否か。
     *
     * @return シングルタップで詳細を表示する場合true
     */
    public boolean shouldShowDeviceDetailOnTap() {
        return mStorage.readBoolean(Key.SHOULD_SHOW_DEVICE_DETAIL_ON_TAP, true);
    }

    /**
     * コンテンツリストのシングルタップで詳細を表示するか否か。
     *
     * @return シングルタップで詳細を表示する場合true
     */
    public boolean shouldShowContentDetailOnTap() {
        return mStorage.readBoolean(Key.SHOULD_SHOW_CONTENT_DETAIL_ON_TAP, true);
    }

    /**
     * 削除削除機能が有効か否か。
     *
     * @return 削除機能が有効なときtrue
     */
    public boolean isDeleteFunctionEnabled() {
        return mStorage.readBoolean(Key.DELETE_FUNCTION_ENABLED, false);
    }

    /**
     * アップデートファイルを取得した時刻を返す。
     *
     * @return アップデートファイルを取得した時刻
     */
    public long getUpdateFetchTime() {
        return mStorage.readLong(Key.UPDATE_FETCH_TIME, 0);
    }

    /**
     * アップデートファイルを取得した時刻を更新する。
     */
    public void setUpdateFetchTime() {
        mStorage.writeLong(Key.UPDATE_FETCH_TIME, System.currentTimeMillis());
    }

    /**
     * アップデートが利用できるか否かを返す。
     *
     * @return アップデートが利用できるときtrue
     */
    public boolean isUpdateAvailable() {
        return mStorage.readBoolean(Key.UPDATE_AVAILABLE, false);
    }

    /**
     * アップデートが利用できるか否かを設定する。
     *
     * @param available アップデートが利用できるときtrue
     */
    public void setUpdateAvailable(final boolean available) {
        mStorage.writeBoolean(Key.UPDATE_AVAILABLE, available);
    }

    /**
     * update.jsonの文字列を返す。
     *
     * @return update.jsonの文字列
     */
    @NonNull
    public String getUpdateJson() {
        return mStorage.readString(Key.UPDATE_JSON, "");
    }

    /**
     * update.jsonの文字列を設定する。
     *
     * @param json update.jsonの文字列
     */
    public void setUpdateJson(@Nullable final String json) {
        mStorage.writeString(Key.UPDATE_JSON, TextUtils.isEmpty(json) ? "" : json);
    }
}
