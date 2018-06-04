/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import okhttp3.OkHttpClient

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object OkHttpClientHolder {
    private val OK_HTTP_CLIENT = OkHttpClient()

    @JvmStatic
    fun get(): OkHttpClient {
        return OK_HTTP_CLIENT
    }
}
