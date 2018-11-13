/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import android.support.annotation.StyleRes

import net.mm2d.preference.Header

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ThemeParams(
    val htmlQuery: String,
    @StyleRes
    val noActionBarThemeId: Int,
    @StyleRes
    val listThemeId: Int,
    @StyleRes
    val fullscreenThemeId: Int,
    @StyleRes
    val settingsThemeId: Int,
    @StyleRes
    val popupThemeId: Int,
    val preferenceHeaderConverter: PreferenceHeaderConverter,
    val themeColorGenerator: ThemeColorGenerator,
    val serverColorExtractor: ServerColorExtractor
) {
    interface PreferenceHeaderConverter {
        fun convert(headers: List<Header>)
    }
}
