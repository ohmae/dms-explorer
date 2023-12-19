/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp

import net.mm2d.upnp.Icon

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object DownloadIcon {
    private const val MIME_JPG = "image/jpeg"
    private const val MIME_PNG = "image/png"

    val COMPARATOR = { i1: Icon, i2: Icon -> calcScore(i1) - calcScore(i2) }

    /**
     * ダウンロードするIconを決定するため、Iconの評価値を計算する
     *
     * この評価値が最も高いIconをダウンロードする。
     * 通常はpngかjpegであり、これ以外はデコードできるか不明なため切り落とす。
     * 規格上最低一つはpng形式であることが要求されているため、
     * サーバ側が仕様を守っていれば、すべてが未対応となることはない。
     * あとは色深度と画像サイズが大きいものほど優先とする。
     * 色深度と画像サイズが同じものがあればpngを優先する。
     *
     * @param icon Icon
     * @return Iconの優先度
     */
    private fun calcScore(icon: Icon): Int {
        val mime = icon.mimeType
        val png = MIME_PNG == mime
        val jpg = MIME_JPG == mime
        if (!png && !jpg) {
            return 0
        }
        val width = icon.width
        val height = icon.height
        val depth = icon.depth
        return width * height * depth + if (png) 1 else 0
    }
}
