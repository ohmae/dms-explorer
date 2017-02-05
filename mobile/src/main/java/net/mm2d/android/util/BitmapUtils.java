/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

/**
 * Bitmapに関連する共通処理をまとめたユーティリティクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */

public class BitmapUtils {
    private BitmapUtils() {
        throw new AssertionError();
    }

    /**
     * 画像データを指定された表示枠に必要最小限の大きさにダウンサンプリングしてBitmapを作成する。
     *
     * <p>画像データをデコードしたサイズが表示する枠よりも十分以上に大きい場合、
     * そのままデコードするのはメモリの無駄でしかなく、OutOfMemoryErrorが発生しかねないため、
     * デコードの段階で表示品質を落とさない最小の大きさにデコードさせる。
     *
     * <p>画像データが表示枠に完全に収まる場合はそのままの大きさでダウンサンプリングする。
     * また、縦横比が異なる場合は縦横比固定で表示枠に内接する大きさに表示させると判断する。
     *
     * <p>ダウンサンプル数は整数でしか指定できないため、
     * 最大で所望のサイズの2倍までの大きさになる可能性がある。
     *
     * @param data   画像データ
     * @param width  表示枠の幅
     * @param height 表示枠の高さ
     * @return Bitmap
     */
    @NonNull
    public static Bitmap decodeBitmap(@NonNull byte[] data, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calcSampleSize(options.outWidth, options.outHeight, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * ダウンサンプリング数を計算する。
     *
     * @param sourceWidth  元画像の幅
     * @param sourceHeight 元画像の高さ
     * @param targetWidth  取得したい枠の幅
     * @param targetHeight 取得したい枠の幅
     * @return ダウンサンプル数
     */
    private static int calcSampleSize(int sourceWidth, int sourceHeight,
                                     int targetWidth, int targetHeight) {
        if (targetWidth == 0 || targetHeight == 0) {
            return 1; // avoid divide by zero
        }
        if (sourceWidth * targetHeight > targetWidth * sourceHeight) {
            // sourceの方が横長なので幅で合わせる
            return sourceWidth > targetWidth ? sourceWidth / targetWidth : 1;
        }
        return sourceHeight > targetHeight ? sourceHeight / targetHeight : 1;
    }
}
