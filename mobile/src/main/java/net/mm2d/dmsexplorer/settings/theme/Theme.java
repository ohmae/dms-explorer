/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.settings.theme.ThemeParams.Builder;
import net.mm2d.preference.Header;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public enum Theme {
    DEFAULT(new Builder()
            .setHtmlQuery("t=default")
            .setThemeId(R.style.AppTheme)
            .setNoActionBarThemeId(R.style.AppTheme_NoActionBar)
            .setListThemeId(R.style.AppTheme_List)
            .setPopupThemeId(R.style.AppTheme_PopupOverlay)
            .setFullscreenThemeId(R.style.AppTheme_FullScreen)
            .setPreferenceHeaderConverter(headers -> {
            })
            .setThemeColorGenerator(new ThemeColorGeneratorDefault())
            .setServerColorExtractor(new ServerColorExtractorDefault())
            .build()),
    DARK(new Builder()
            .setHtmlQuery("t=dark")
            .setThemeId(R.style.DarkTheme)
            .setNoActionBarThemeId(R.style.DarkTheme_NoActionBar)
            .setListThemeId(R.style.DarkTheme_List)
            .setPopupThemeId(R.style.DarkTheme_PopupOverlay)
            .setFullscreenThemeId(R.style.DarkTheme_FullScreen)
            .setPreferenceHeaderConverter(headers -> {
                for (final Header header : headers) {
                    header.iconRes = convertIcon(header.iconRes);
                }
            })
            .setThemeColorGenerator(new ThemeColorGeneratorDark())
            .setServerColorExtractor(new ServerColorExtractorDark())
            .build()),;

    @DrawableRes
    private static int convertIcon(@DrawableRes final int iconRes) {
        switch (iconRes) {
            case R.drawable.ic_play_settings_light:
                return R.drawable.ic_play_settings_dark;
            case R.drawable.ic_function_settings_light:
                return R.drawable.ic_function_settings_dark;
            case R.drawable.ic_view_settings_light:
                return R.drawable.ic_view_settings_dark;
            case R.drawable.ic_expert_settings_light:
                return R.drawable.ic_expert_settings_dark;
            case R.drawable.ic_info_settings_light:
                return R.drawable.ic_info_settings_dark;
            // for 4.x
            case R.drawable.ic_play_settings_black:
                return R.drawable.ic_play_settings_white;
            case R.drawable.ic_function_settings_black:
                return R.drawable.ic_function_settings_white;
            case R.drawable.ic_view_settings_black:
                return R.drawable.ic_view_settings_white;
            case R.drawable.ic_expert_settings_black:
                return R.drawable.ic_expert_settings_white;
            case R.drawable.ic_info_settings_black:
                return R.drawable.ic_info_settings_white;
        }
        return iconRes;
    }

    private final ThemeParams mParams;

    Theme(@NonNull final ThemeParams params) {
        mParams = params;
    }

    @NonNull
    public ThemeParams getParams() {
        return mParams;
    }
}
