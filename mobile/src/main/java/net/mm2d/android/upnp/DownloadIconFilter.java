/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp;

import android.support.annotation.NonNull;

import net.mm2d.upnp.Icon;
import net.mm2d.upnp.IconFilter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class DownloadIconFilter implements IconFilter {
    private static final String MIME_JPG = "image/jpeg";
    private static final String MIME_PNG = "image/png";

    /**
     * ダウンロードするIconを決定するため、Iconの評価値を計算する
     *
     * <p>この評価値が最も高いIconをダウンロードする。
     * 通常はpngかjpegであり、これ以外はデコードできるか不明なため切り落とす。
     * 規格上最低一つはpng形式であることが要求されているため、
     * サーバ側が仕様を守っていれば、すべてが未対応となることはない。
     * あとは色深度と画像サイズが大きいものほど優先とする。
     * 色深度と画像サイズが同じものがあればpngを優先する。
     *
     * @param icon Icon
     * @return Iconの優先度
     */
    private static int calcScore(@NonNull final Icon icon) {
        final String mime = icon.getMimeType();
        final boolean png = MIME_PNG.equals(mime);
        final boolean jpg = MIME_JPG.equals(mime);
        if (!png && !jpg) {
            return 0;
        }
        final int width = icon.getWidth();
        final int height = icon.getHeight();
        final int depth = icon.getDepth();
        return width * height * depth + (png ? 1 : 0);
    }

    private static final Comparator<Icon> ICON_COMPARATOR = (i1, i2) -> calcScore(i1) - calcScore(i2);

    @NonNull
    @Override
    public List<Icon> filter(final @NonNull List<Icon> list) {
        return Collections.singletonList(Collections.max(list, ICON_COMPARATOR));
    }
}
