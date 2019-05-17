/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import net.mm2d.log.Logger

/**
 * 他のアプリを起動させるための定形処理をまとめたユーティリティクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object LaunchUtils {
    /**
     * URIを指定して暗黙的Intentによって他のアプリを起動する。
     *
     * 処理できるアプリケーションが存在しない場合はExceptionをcatchし、
     * 戻り値で結果を通知する。
     *
     * @param context コンテキスト
     * @param uri     URI
     * @return 起動ができた場合true、何らかの理由で起動できない場合false
     */
    @JvmStatic
    fun openUri(context: Context, uri: String?): Boolean {
        if (uri.isNullOrEmpty()) return false
        try {
            Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .let {
                    context.startActivity(it)
                }
        } catch (e: ActivityNotFoundException) {
            Logger.w(e)
            return false
        }
        return true
    }

    /**
     * 指定したパッケージのアプリを指定してPlayストアを開く。
     *
     * @param context     コンテキスト
     * @param packageName 開くアプリのパッケージ名
     * @return 起動ができた場合true、何らかの理由で起動できない場合false
     */
    @JvmStatic
    fun openGooglePlay(context: Context, packageName: String?): Boolean {
        return openUri(context, "market://details?id=$packageName")
    }
}
