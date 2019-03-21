/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.util.SparseArray;

import net.mm2d.dmsexplorer.R;

import java.util.Map;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.collection.ArrayMap;

/**
 * 連続再生モードを表現するenum。
 *
 * <p>設定としてname()の文字列を保存しているため、シンボル名の変更を行う場合はマイグレーション処理必須
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public enum RepeatMode {
    /**
     * １項目のみ再生。
     */
    PLAY_ONCE(
            0,
            R.drawable.ic_play_once,
            R.string.toast_repeat_play_once
    ),
    /**
     * フォルダ内を最後まで連続再生。
     */
    SEQUENTIAL(
            1,
            R.drawable.ic_sequential,
            R.string.toast_repeat_sequential
    ),
    /**
     * 全体をループ再生。
     */
    REPEAT_ALL(
            2,
            R.drawable.ic_repeat_all,
            R.string.toast_repeat_repeat_all
    ),
    /**
     * １項目をループ再生。
     */
    REPEAT_ONE(
            3,
            R.drawable.ic_repeat_one,
            R.string.toast_repeat_repeat_one
    ),
    ;
    private final int mOrder;
    private final int mIconId;
    private final int mMessageId;

    /**
     * コンストラクタ。
     *
     * @param order     トグル順序
     * @param iconId    モードアイコンのID
     * @param messageId Toastで表示するメッセージのID
     */
    RepeatMode(
            int order,
            @DrawableRes int iconId,
            @StringRes int messageId) {
        mOrder = order;
        mIconId = iconId;
        mMessageId = messageId;
    }

    /**
     * 順序を返す。
     *
     * @return 順序
     */
    private int getOrder() {
        return mOrder;
    }

    /**
     * アイコンのリソースIDを返す。
     *
     * @return アイコンのリソースID
     */
    @DrawableRes
    public int getIconId() {
        return mIconId;
    }

    /**
     * Toastで表示するメッセージのIDを返す。
     *
     * @return メッセージのID
     */
    @StringRes
    public int getMessageId() {
        return mMessageId;
    }

    /**
     * トグルしたときの次のモードを返す。
     *
     * @return 次のモード
     */
    @NonNull
    public RepeatMode next() {
        return of((getOrder() + 1) % sLength);
    }

    private static final SparseArray<RepeatMode> sOrderMap;
    private static final Map<String, RepeatMode> sNameMap;
    private static final int sLength;

    static {
        final RepeatMode[] values = values();
        sLength = values.length;
        sOrderMap = new SparseArray<>();
        sNameMap = new ArrayMap<>(sLength);
        for (final RepeatMode mode : values) {
            sOrderMap.put(mode.getOrder(), mode);
            sNameMap.put(mode.name(), mode);
        }
    }

    /**
     * 順序値から該当するEnum値を返す。
     *
     * @param value 順序値
     * @return 該当するEnum値、該当するものがない場合デフォルト値
     */
    @NonNull
    private static RepeatMode of(int value) {
        return sOrderMap.get(value, PLAY_ONCE);
    }

    /**
     * モードを表現する文字列から該当するEnum値を返す。
     *
     * @param value モードを表現する文字列
     * @return 該当するEnum、該当するものがない場合デフォルト値
     */
    @NonNull
    public static RepeatMode of(String value) {
        final RepeatMode mode = sNameMap.get(value);
        return mode != null ? mode : PLAY_ONCE;
    }
}
