/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.SparseArray;

import net.mm2d.dmsexplorer.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public enum RepeatMode {
    PLAY_ONCE(0, R.drawable.ic_play_once, R.string.toast_repeat_play_once),
    SEQUENTIAL(1, R.drawable.ic_sequential, R.string.toast_repeat_sequential),
    REPEAT_ALL(2, R.drawable.ic_repeat_all, R.string.toast_repeat_repeat_all),
    REPEAT_ONE(3, R.drawable.ic_repeat_one, R.string.toast_repeat_repeat_one),;
    private final int mOrder;
    private final int mIconId;
    private final int mMessageId;

    RepeatMode(int order, @DrawableRes int iconId, @StringRes int messageId) {
        mOrder = order;
        mIconId = iconId;
        mMessageId = messageId;
    }

    public int getOrder() {
        return mOrder;
    }

    @DrawableRes
    public int getIconId() {
        return mIconId;
    }

    @StringRes
    public int getMessageId() {
        return mMessageId;
    }

    public RepeatMode next() {
        return of((getOrder() + 1) % sLength);
    }

    private static final SparseArray<RepeatMode> sOrderMap;
    private static final Map<String, RepeatMode> sNameMap;
    private static final int sLength = values().length;

    static {
        sOrderMap = new SparseArray<>();
        sNameMap = new HashMap<>();
        for (final RepeatMode mode : values()) {
            sOrderMap.put(mode.getOrder(), mode);
            sNameMap.put(mode.name(), mode);
        }
    }

    @NonNull
    private static RepeatMode of(int value) {
        return sOrderMap.get(value, PLAY_ONCE);
    }

    @NonNull
    public static RepeatMode of(String value) {
        final RepeatMode mode = sNameMap.get(value);
        return mode != null ? mode : PLAY_ONCE;
    }
}
