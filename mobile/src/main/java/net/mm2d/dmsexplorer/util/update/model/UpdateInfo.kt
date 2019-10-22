/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@JsonClass(generateAdapter = true)
internal data class UpdateInfo(
    @Json(name = "mobile")
    internal val mobile: Mobile
) {
    val isValid: Boolean
        get() = mobile.versionName.isNotEmpty()
            && mobile.versionCode != 0
            && mobile.targetInclude.isNotEmpty()

    @JsonClass(generateAdapter = true)
    internal data class Mobile(
        @Json(name = "versionName")
        internal val versionName: String,
        @Json(name = "versionCode")
        internal val versionCode: Int,
        @Json(name = "targetInclude")
        internal val targetInclude: List<Int>,
        @Json(name = "targetExclude")
        internal val targetExclude: List<Int>,
        @Json(name = "minSdkVersion")
        internal val minSdkVersion: Int = 0
    )

    companion object {
        internal val EMPTY_UPDATE_INFO = UpdateInfo(Mobile("", 0, listOf(0), emptyList()))
    }
}
