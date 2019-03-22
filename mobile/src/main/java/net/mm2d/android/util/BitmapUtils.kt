/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * Bitmapに関連する共通処理をまとめたユーティリティクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object BitmapUtils {
    /**
     * 画像データを指定された表示枠に必要最小限の大きさにダウンサンプリングしてBitmapを作成する。
     *
     * 画像データをデコードしたサイズが表示する大きさより十分に大きい場合、
     * そのままデコードするのはメモリの無駄でしかなく、OutOfMemoryErrorが発生しかねないため、
     * デコードの段階で表示品質を落とさない最小の大きさにデコードさせる。
     *
     * 画像データが表示枠に完全に収まる場合はそのままの大きさでサンプリングする。
     * また、縦横比が異なる場合は縦横比固定で表示枠に内接する大きさに表示させると判断する。
     *
     * ダウンサンプル数は整数でしか指定できないため、
     * 最大で所望のサイズの2倍までの大きさになる可能性がある。
     *
     * @param data   画像データ
     * @param width  表示枠の幅
     * @param height 表示枠の高さ
     * @return Bitmap
     */
    @JvmStatic
    fun decode(data: ByteArray, width: Int, height: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().also {
                it.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(data, 0, data.size, it)
                it.inSampleSize = calcSampleSize(it.outWidth, it.outHeight, width, height)
                it.inJustDecodeBounds = false
            }
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (ignored: Exception) {
            null
        }
    }

    /**
     * 指定サイズに内接する画像をレンダリングするのに必要なサンプリング数を計算する。
     *
     * ドキュメントに
     * Any value <= 1 is treated the same as 1.
     * とあるため、1未満にならないような処理は省略している。
     *
     * @param sourceWidth  元画像の幅
     * @param sourceHeight 元画像の高さ
     * @param targetWidth  取得したい枠の幅
     * @param targetHeight 取得したい枠の幅
     * @return ダウンサンプル数
     */
    private fun calcSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        // sourceの方が横長なら幅で合わせる、縦長なら高さで合わせる
        return if (targetWidth == 0 || targetHeight == 0) {
            1 // Avoid zero division
        } else if (sourceWidth * targetHeight > targetWidth * sourceHeight) {
            sourceWidth / targetWidth
        } else {
            sourceHeight / targetHeight
        }
    }

    /**
     * EXIFの向き情報が取得可能であれば、それに基づき回転、反転を加えた上で、
     * 画像データを指定された表示枠に必要最小限の大きさにダウンサンプリングしてBitmapを作成する。
     *
     * EXIF情報から向き情報が取得可能な場合、その情報を加味して回転・反転を行う。
     * 回転が行われる場合は、ダウンサンプリングの計算もそれに合わせて行う。
     *
     * @param data   画像データ
     * @param width  表示枠の幅
     * @param height 表示枠の高さ
     * @return Bitmap
     * @see .decode
     */
    @JvmStatic
    fun decodeWithOrientation(data: ByteArray, width: Int, height: Int): Bitmap? {
        return decode(data, width, height, extractOrientation(data))
    }

    private fun extractOrientation(data: ByteArray): Int {
        try {
            return ExifInterface(ByteArrayInputStream(data)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        } catch (ignored: IOException) {
        }
        return ExifInterface.ORIENTATION_UNDEFINED
    }

    private fun decode(data: ByteArray, width: Int, height: Int, orientation: Int): Bitmap? {
        val base = decodeBase(data, width, height, orientation) ?: return null
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_ROTATE_90 ->
                matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 ->
                matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 ->
                matrix.postRotate(270f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_NORMAL,
            ExifInterface.ORIENTATION_UNDEFINED ->
                return base
            else ->
                return base
        }
        return Bitmap.createBitmap(base, 0, 0, base.width, base.height, matrix, true)
    }

    private fun decodeBase(binary: ByteArray, width: Int, height: Int, orientation: Int): Bitmap? {
        return when (orientation) {
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSVERSE,
            ExifInterface.ORIENTATION_ROTATE_270 ->
                decode(binary, height, width)
            ExifInterface.ORIENTATION_UNDEFINED,
            ExifInterface.ORIENTATION_NORMAL,
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_ROTATE_180,
            ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                decode(binary, width, height)
            else ->
                decode(binary, width, height)
        }
    }
}
