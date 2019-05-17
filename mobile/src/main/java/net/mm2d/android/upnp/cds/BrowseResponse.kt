/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class BrowseResponse(
    private val response: Map<String, String>
) {

    val numberReturned: Int
        get() = response[NUMBER_RETURNED]?.toIntOrNull() ?: -1

    val totalMatches: Int
        get() = response[TOTAL_MATCHES]?.toIntOrNull() ?: -1

    val updateId: Int
        get() = response[UPDATE_ID]?.toIntOrNull() ?: -1

    val result: String?
        get() = response[RESULT]

    companion object {
        private const val RESULT = "Result"
        private const val NUMBER_RETURNED = "NumberReturned"
        private const val TOTAL_MATCHES = "TotalMatches"
        private const val UPDATE_ID = "UpdateID"
    }
}
