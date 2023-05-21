/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme

import androidx.annotation.DrawableRes
import net.mm2d.dmsexplorer.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
enum class Theme(val params: ThemeParams) {
    DEFAULT(
        ThemeParams(
            htmlQuery = "t=default",
            noActionBarThemeId = R.style.AppTheme_NoActionBar,
            listThemeId = R.style.AppTheme_List,
            popupThemeId = R.style.AppTheme_PopupOverlay,
            fullscreenThemeId = R.style.AppTheme_FullScreen,
            settingsThemeId = R.style.AppTheme_Settings,
            preferenceHeaderConverter = { },
            themeColorGenerator = ThemeColorGeneratorDefault(),
            serverColorExtractor = ServerColorExtractorDefault()
        )
    ),
    DARK(
        ThemeParams(
            htmlQuery = "t=dark",
            noActionBarThemeId = R.style.DarkTheme_NoActionBar,
            listThemeId = R.style.DarkTheme_List,
            popupThemeId = R.style.DarkTheme_PopupOverlay,
            fullscreenThemeId = R.style.DarkTheme_FullScreen,
            settingsThemeId = R.style.DarkTheme_Settings,
            preferenceHeaderConverter = { headers ->
                for (header in headers) {
                    header.iconRes = convertIcon(header.iconRes)
                }
            },
            themeColorGenerator = ThemeColorGeneratorDark(),
            serverColorExtractor = ServerColorExtractorDark()
        )
    )
}

@DrawableRes
private fun convertIcon(@DrawableRes iconRes: Int): Int = when (iconRes) {
    R.drawable.ic_play_settings_light -> R.drawable.ic_play_settings_dark
    R.drawable.ic_function_settings_light -> R.drawable.ic_function_settings_dark
    R.drawable.ic_view_settings_light -> R.drawable.ic_view_settings_dark
    R.drawable.ic_expert_settings_light -> R.drawable.ic_expert_settings_dark
    R.drawable.ic_info_settings_light -> R.drawable.ic_info_settings_dark
    // for 4.x
    R.drawable.ic_play_settings_black -> R.drawable.ic_play_settings_white
    R.drawable.ic_function_settings_black -> R.drawable.ic_function_settings_white
    R.drawable.ic_view_settings_black -> R.drawable.ic_view_settings_white
    R.drawable.ic_expert_settings_black -> R.drawable.ic_expert_settings_white
    R.drawable.ic_info_settings_black -> R.drawable.ic_info_settings_white
    else -> iconRes
}
