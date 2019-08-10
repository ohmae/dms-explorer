/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object OkHttpClientHolder {
    private val interceptors: MutableList<Interceptor> = mutableListOf()
    private val client by lazy {
        val builder = OkHttpClient.Builder()
        interceptors.forEach { builder.addInterceptor(it) }
        builder.build()
    }

    fun addNetworkInterceptor(interceptor: Interceptor) {
        interceptors += interceptor
    }

    fun get(): OkHttpClient = client
}
