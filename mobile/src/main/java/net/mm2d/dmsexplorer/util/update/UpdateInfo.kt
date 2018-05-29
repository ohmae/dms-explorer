/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update

import com.squareup.moshi.Json

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal data class UpdateInfo(
        @Json(name = "mobile")
        private val mobile: Mobile
) {
    val versionCode: Int
        get() = mobile.versionCode

    val versionName: String
        get() = mobile.versionName

    val targetInclude: List<Int>
        get() = mobile.targetInclude

    val targetExclude: List<Int>
        get() = mobile.targetExclude

    internal data class Mobile(
            @Json(name = "versionName")
            internal val versionName: String,
            @Json(name = "versionCode")
            internal val versionCode: Int,
            @Json(name = "targetInclude")
            internal val targetInclude: List<Int>,
            @Json(name = "targetExclude")
            internal val targetExclude: List<Int>
    )
}
