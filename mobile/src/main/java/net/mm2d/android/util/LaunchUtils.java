/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.util.Log;

/**
 * 他のアプリを起動させるための定形処理をまとめたユーティリティクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class LaunchUtils {
    private static final String TAG = "LaunchUtils";

    /**
     * URIを指定して暗黙的Intentによって他のアプリを起動する。
     *
     * <p>処理できるアプリケーションが存在しない場合はExceptionをcatchし、
     * 戻り値で結果を通知する。
     *
     * @param context コンテキスト
     * @param uri     URI
     * @return 起動ができた場合true、何らかの理由で起動できない場合false
     */
    public static boolean openUri(@NonNull final Context context, @Nullable final String uri) {
        if (uri == null) {
            return false;
        }
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            Log.w(TAG, e);
            return false;
        }
        return true;
    }
}
