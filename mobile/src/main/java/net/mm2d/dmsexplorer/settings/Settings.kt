/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Looper
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import net.mm2d.dmsexplorer.BuildConfig
import net.mm2d.dmsexplorer.domain.entity.ContentType
import net.mm2d.dmsexplorer.settings.theme.Theme
import net.mm2d.dmsexplorer.settings.theme.ThemeParams
import net.mm2d.log.Logger
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * SharedPreferencesに覚えさせる設定値を集中管理するクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 * @constructor
 * @param storage SettingsStorage
 */
class Settings private constructor(
    private val storage: SettingsStorage
) {
    /**
     * 動画再生をアプリで行うか否か
     */
    private val isPlayMovieMyself: Boolean
        get() = storage.readBoolean(Key.PLAY_MOVIE_MYSELF)

    /**
     * 音楽再生をアプリで行うか否か
     */
    private val isPlayMusicMyself: Boolean
        get() = storage.readBoolean(Key.PLAY_MUSIC_MYSELF)

    /**
     * 静止画再生をアプリで行うか否か
     */
    private val isPlayPhotoMyself: Boolean
        get() = storage.readBoolean(Key.PLAY_PHOTO_MYSELF)

    /**
     * 動画再生のリピートモード
     */
    var repeatModeMovie: RepeatMode
        get() = RepeatMode.of(storage.readString(Key.REPEAT_MODE_MOVIE))
        set(mode) = storage.writeString(Key.REPEAT_MODE_MOVIE, mode.name)

    /**
     * 音楽再生のリピートモード
     */
    var repeatModeMusic: RepeatMode
        get() = RepeatMode.of(storage.readString(Key.REPEAT_MODE_MUSIC))
        set(mode) = storage.writeString(Key.REPEAT_MODE_MUSIC, mode.name)

    /**
     * リピートモードの操作案内を表示したか否か
     */
    val isRepeatIntroduced: Boolean
        get() = storage.readBoolean(Key.REPEAT_INTRODUCED)

    /**
     * 削除削除機能が有効か否か
     */
    val isDeleteFunctionEnabled: Boolean
        get() = storage.readBoolean(Key.DELETE_FUNCTION_ENABLED)

    /**
     * アップデートファイルを取得した時刻
     */
    val updateFetchTime: Long
        get() = storage.readLong(Key.UPDATE_FETCH_TIME)

    /**
     * アップデートが利用できるか否か
     */
    var isUpdateAvailable: Boolean
        get() = storage.readBoolean(Key.UPDATE_AVAILABLE)
        set(available) = storage.writeBoolean(Key.UPDATE_AVAILABLE, available)

    /**
     * update.jsonの文字列
     */
    var updateJson: String
        get() = storage.readString(Key.UPDATE_JSON)
        set(json) = storage.writeString(Key.UPDATE_JSON, if (json.isEmpty()) "" else json)

    /**
     * ブラウズ画面の画面の向き設定を返す。
     *
     * @return ブラウズ画面の画面の向き設定
     */
    val browseOrientation: Orientation
        get() = Orientation.of(storage.readString(Key.ORIENTATION_BROWSE))

    /**
     * 動画画面の画面の向き設定を返す。
     *
     * @return 動画画面の画面の向き設定
     */
    val movieOrientation: Orientation
        get() = Orientation.of(storage.readString(Key.ORIENTATION_MOVIE))

    /**
     * 音楽画面の画面の向き設定を返す。
     *
     * @return 音楽画面の画面の向き設定
     */
    val musicOrientation: Orientation
        get() = Orientation.of(storage.readString(Key.ORIENTATION_MUSIC))

    /**
     * 画面の画面の向き設定を返す。
     *
     * @return ブラウズ画面の画面の向き設定
     */
    val photoOrientation: Orientation
        get() = Orientation.of(storage.readString(Key.ORIENTATION_PHOTO))

    /**
     * DMC画面の画面の向き設定を返す。
     *
     * @return DMC画面の画面の向き設定
     */
    val dmcOrientation: Orientation
        get() = Orientation.of(storage.readString(Key.ORIENTATION_DMC))

    /**
     * 動画UIの背景を透明にするか
     *
     * @return 動画UIの背景を透明にするときtrue
     */
    val isMovieUiBackgroundTransparent: Boolean
        get() = storage.readBoolean(Key.IS_MOVIE_UI_BACKGROUND_TRANSPARENT)

    /**
     * 静止画UIの背景を透明にするか
     *
     * @return 静止画UIの背景を透明にするときtrue
     */
    val isPhotoUiBackgroundTransparent: Boolean
        get() = storage.readBoolean(Key.IS_PHOTO_UI_BACKGROUND_TRANSPARENT)

    val themeParams: ThemeParams
        get() {
            val dark = storage.readBoolean(Key.DARK_THEME)
            return (if (dark) Theme.DARK else Theme.DEFAULT).params
        }

    var logSendTime: Long
        get() = storage.readLong(Key.LOG_SEND_TIME)
        set(time) = storage.writeLong(Key.LOG_SEND_TIME, time)

    val dump: Bundle
        get() {
            val keys = listOf(
                Key.PLAY_MOVIE_MYSELF,
                Key.PLAY_MUSIC_MYSELF,
                Key.PLAY_PHOTO_MYSELF,
                Key.USE_CUSTOM_TABS,
                Key.SHOULD_SHOW_DEVICE_DETAIL_ON_TAP,
                Key.SHOULD_SHOW_CONTENT_DETAIL_ON_TAP,
                Key.DELETE_FUNCTION_ENABLED,
                Key.DARK_THEME,
                Key.DO_NOT_SHOW_MOVIE_UI_ON_START,
                Key.DO_NOT_SHOW_MOVIE_UI_ON_TOUCH,
                Key.DO_NOT_SHOW_TITLE_IN_MOVIE_UI,
                Key.IS_MOVIE_UI_BACKGROUND_TRANSPARENT,
                Key.DO_NOT_SHOW_PHOTO_UI_ON_START,
                Key.DO_NOT_SHOW_PHOTO_UI_ON_TOUCH,
                Key.DO_NOT_SHOW_TITLE_IN_PHOTO_UI,
                Key.IS_PHOTO_UI_BACKGROUND_TRANSPARENT,
                Key.ORIENTATION_BROWSE,
                Key.ORIENTATION_MOVIE,
                Key.ORIENTATION_MUSIC,
                Key.ORIENTATION_PHOTO,
                Key.ORIENTATION_DMC,
                Key.REPEAT_MODE_MOVIE,
                Key.REPEAT_MODE_MUSIC
            )
            val bundle = Bundle()
            keys.forEach {
                when {
                    it.isBooleanKey ->
                        bundle.putString(it.name, if (storage.readBoolean(it)) "on" else "off")
                    it.isIntKey ->
                        bundle.putString(it.name, storage.readInt(it).toString())
                    it.isLongKey ->
                        bundle.putString(it.name, storage.readLong(it).toString())
                    it.isStringKey ->
                        bundle.putString(it.name, storage.readString(it))
                }
            }
            return bundle
        }

    /**
     * 指定コンテンツ種別をアプリで再生するか否かを返す。
     *
     * @param type コンテンツ種別
     * @return アプリで再生する場合true
     */
    fun isPlayMyself(type: ContentType): Boolean {
        return when (type) {
            ContentType.MOVIE -> isPlayMovieMyself
            ContentType.MUSIC -> isPlayMusicMyself
            ContentType.PHOTO -> isPlayPhotoMyself
            else -> false
        }
    }

    /**
     * リピートモードの操作案内を表示した。
     */
    fun notifyRepeatIntroduced() {
        storage.writeBoolean(Key.REPEAT_INTRODUCED, true)
    }

    /**
     * Chrome Custom Tabsを使用するか否か。
     *
     * @return 使用する場合true
     */
    fun useCustomTabs(): Boolean {
        return storage.readBoolean(Key.USE_CUSTOM_TABS)
    }

    /**
     * デバイスリストのシングルタップで詳細を表示するか否か。
     *
     * @return シングルタップで詳細を表示する場合true
     */
    fun shouldShowDeviceDetailOnTap(): Boolean {
        return storage.readBoolean(Key.SHOULD_SHOW_DEVICE_DETAIL_ON_TAP)
    }

    /**
     * コンテンツリストのシングルタップで詳細を表示するか否か。
     *
     * @return シングルタップで詳細を表示する場合true
     */
    fun shouldShowContentDetailOnTap(): Boolean {
        return storage.readBoolean(Key.SHOULD_SHOW_CONTENT_DETAIL_ON_TAP)
    }

    /**
     * アップデートファイルを取得した時刻を更新する。
     */
    fun setUpdateFetchTime() {
        storage.writeLong(Key.UPDATE_FETCH_TIME, System.currentTimeMillis())
    }

    /**
     * 動画再生の最初にUIを表示するか
     *
     * @return 動画再生の最初にUIを表示するときtrue
     */
    fun shouldShowMovieUiOnStart(): Boolean {
        return !storage.readBoolean(Key.DO_NOT_SHOW_MOVIE_UI_ON_START)
    }

    /**
     * タッチしたときに動画UIを表示するか
     *
     * @return タッチしたときに動画UIを表示するときtrue
     */
    fun shouldShowMovieUiOnTouch(): Boolean {
        return !storage.readBoolean(Key.DO_NOT_SHOW_MOVIE_UI_ON_TOUCH)
    }

    /**
     * 動画UIでコンテンツタイトルを表示するか
     *
     * @return 動画UIでコンテンツタイトルを表示するときtrue
     */
    fun shouldShowTitleInMovieUi(): Boolean {
        return !storage.readBoolean(Key.DO_NOT_SHOW_TITLE_IN_MOVIE_UI)
    }

    /**
     * 静止画再生の最初にUIを表示するか
     *
     * @return 静止画再生の最初にUIを表示するときtrue
     */
    fun shouldShowPhotoUiOnStart(): Boolean {
        return !storage.readBoolean(Key.DO_NOT_SHOW_PHOTO_UI_ON_START)
    }

    /**
     * タッチしたときに静止画UIを表示するか
     *
     * @return タッチしたときに静止画UIを表示するときtrue
     */
    fun shouldShowPhotoUiOnTouch(): Boolean {
        return !storage.readBoolean(Key.DO_NOT_SHOW_PHOTO_UI_ON_TOUCH)
    }

    /**
     * 静止画UIでコンテンツタイトルを表示するか
     *
     * @return 静止画UIでコンテンツタイトルを表示するときtrue
     */
    fun shouldShowTitleInPhotoUi(): Boolean {
        return !storage.readBoolean(Key.DO_NOT_SHOW_TITLE_IN_PHOTO_UI)
    }

    companion object {
        private var settings: Settings? = null
        private val lock = ReentrantLock()
        private val condition = lock.newCondition()
        private val mainThread = Looper.getMainLooper().thread

        fun get(): Settings {
            lock.withLock {
                while (settings == null) {
                    if (BuildConfig.DEBUG && Thread.currentThread() === mainThread) {
                        Logger.e("!!!!!!!!!! BLOCK !!!!!!!!!!")
                    }
                    try {
                        if (!condition.await(1, TimeUnit.SECONDS)) {
                            throw IllegalStateException("Settings initialization timeout")
                        }
                    } catch (e: InterruptedException) {
                        Logger.w(e)
                    }
                }
                return settings!!
            }
        }

        /**
         * アプリ起動時に一度だけコールされ、初期化を行う。
         *
         * @param context コンテキスト
         */
        @SuppressLint("CheckResult")
        fun initialize(context: Context) {
            Completable.fromAction { initializeInner(context) }
                .subscribeOn(Schedulers.io())
                .subscribe()
        }

        private fun initializeInner(context: Context) {
            val storage = SettingsStorage(context)
            Maintainer.maintain(storage)
            lock.withLock {
                settings = Settings(storage)
                condition.signalAll()
            }
        }
    }
}
