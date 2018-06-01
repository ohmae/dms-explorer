/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Bitmapに関連する共通処理をまとめたユーティリティクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */

public class BitmapUtils {
    /**
     * 画像データを指定された表示枠に必要最小限の大きさにダウンサンプリングしてBitmapを作成する。
     *
     * <p>画像データをデコードしたサイズが表示する大きさより十分に大きい場合、
     * そのままデコードするのはメモリの無駄でしかなく、OutOfMemoryErrorが発生しかねないため、
     * デコードの段階で表示品質を落とさない最小の大きさにデコードさせる。
     *
     * <p>画像データが表示枠に完全に収まる場合はそのままの大きさでサンプリングする。
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
    @Nullable
    public static Bitmap decode(
            @NonNull final byte[] data,
            final int width,
            final int height) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            options.inSampleSize = calcSampleSize(options.outWidth, options.outHeight, width, height);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (final Exception ignored) {
            return null;
        }
    }

    /**
     * サンプリング数を計算する。
     *
     * @param sourceWidth  元画像の幅
     * @param sourceHeight 元画像の高さ
     * @param targetWidth  取得したい枠の幅
     * @param targetHeight 取得したい枠の幅
     * @return ダウンサンプル数
     */
    private static int calcSampleSize(
            final int sourceWidth,
            final int sourceHeight,
            final int targetWidth,
            final int targetHeight) {
        if (targetWidth == 0 || targetHeight == 0) {
            return 1; // Avoid zero division
        }
        if (sourceWidth * targetHeight > targetWidth * sourceHeight) {
            // sourceの方が横長なので幅で合わせる
            return sourceWidth > targetWidth ? sourceWidth / targetWidth : 1;
        }
        return sourceHeight > targetHeight ? sourceHeight / targetHeight : 1;
    }

    /**
     * EXIFの向き情報が取得可能であれば、それに基づき回転、反転を加えた上で、
     * 画像データを指定された表示枠に必要最小限の大きさにダウンサンプリングしてBitmapを作成する。
     *
     * <p>EXIF情報から向き情報が取得可能な場合、その情報を加味して回転・反転を行う。
     * 回転が行われる場合は、ダウンサンプリングの計算もそれに合わせて行う。
     *
     * @param data   画像データ
     * @param width  表示枠の幅
     * @param height 表示枠の高さ
     * @return Bitmap
     * @see #decode(byte[], int, int)
     */
    @Nullable
    public static Bitmap decodeWithOrientation(
            @NonNull final byte[] data,
            final int width,
            final int height) {
        final int orientation = extractOrientation(data);
        return decode(data, width, height, orientation);
    }

    private static int extractOrientation(@NonNull final byte[] data) {
        final ExifInterface exif;
        try {
            exif = new ExifInterface(new ByteArrayInputStream(data));
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (final IOException ignored) {
        }
        return ExifInterface.ORIENTATION_UNDEFINED;
    }

    @Nullable
    private static Bitmap decode(
            @NonNull final byte[] data,
            final int width,
            final int height,
            final int orientation) {
        final Bitmap base = decodeBase(data, width, height, orientation);
        if (base == null) {
            return null;
        }
        final Matrix matrix = new Matrix();
        switch (orientation) {
            default:
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
                return base;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(270f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270f);
                break;
        }
        return Bitmap.createBitmap(base, 0, 0, base.getWidth(), base.getHeight(), matrix, true);
    }

    @Nullable
    private static Bitmap decodeBase(
            @NonNull final byte[] binary,
            final int width,
            final int height,
            final int orientation) {
        switch (orientation) {
            default:
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return decode(binary, width, height);
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_TRANSVERSE:
            case ExifInterface.ORIENTATION_ROTATE_270:
                //noinspection SuspiciousNameCombination
                return decode(binary, height, width);
        }
    }
}
