/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.content.Context

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface OpenUriModel {
    fun openUri(
        context: Context,
        uri: String,
    )

    fun setUseCustomTabs(
        use: Boolean,
    )

    fun mayLaunchUrl(
        url: String,
    )

    fun mayLaunchUrl(
        urls: List<String>,
    )
}
