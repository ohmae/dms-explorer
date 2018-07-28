/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import io.reactivex.Single
import net.mm2d.upnp.Http
import net.mm2d.upnp.HttpClient
import java.io.IOException
import java.net.URL

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object Downloader {
    @JvmStatic
    fun create(url: String): Single<ByteArray> {
        return Single.create { emitter ->
            val binary = download(url)
            if (binary != null && binary.isNotEmpty()) {
                emitter.onSuccess(binary)
            } else {
                emitter.onError(IOException())
            }
        }
    }

    private fun download(url: String): ByteArray? {
        try {
            val response = HttpClient(false).download(URL(url))
            if (response.status == Http.Status.HTTP_OK) {
                return response.bodyBinary
            }
        } catch (ignored: IOException) {
        } catch (ignored: OutOfMemoryError) {
        }
        return null
    }
}
