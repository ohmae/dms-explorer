/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.graphics.Color;
import android.text.TextUtils;

/**
 * テーマとしての色を決定するメソッドを持つクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ThemeUtils {
    /**
     * リストアイテムのアクセントカラーをタイトルの文字から決定する。
     *
     * @param title アイテムのタイトル
     * @return 先頭の文字から決定したアクセントカラー
     */
    public static int getAccentColor(String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        final float[] hsv = new float[3];
        hsv[0] = (59 * c) % 360;
        hsv[1] = 185f / 255f;
        hsv[2] = 187f / 255f;
        return Color.HSVToColor(hsv);
    }

    /**
     * アクセントカラーより少し暗い色をタイトルの文字から決定する。
     *
     * @param title アイテムのタイトル
     * @return 先頭の文字から決定したアクセントカラー
     */
    public static int getAccentDarkColor(String title) {
        final char c = TextUtils.isEmpty(title) ? ' ' : title.charAt(0);
        final float[] hsv = new float[3];
        hsv[0] = (59 * c) % 360;
        hsv[1] = 185f / 255f;
        hsv[2] = 147f / 255f;
        return Color.HSVToColor(hsv);
    }
}
