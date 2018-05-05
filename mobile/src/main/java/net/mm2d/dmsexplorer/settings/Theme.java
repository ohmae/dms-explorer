/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings;

import android.preference.PreferenceActivity.Header;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.settings.theme.ServerColorExtractorDark;
import net.mm2d.dmsexplorer.settings.theme.ServerColorExtractorNormal;
import net.mm2d.dmsexplorer.settings.theme.ThemeColorGeneratorDark;
import net.mm2d.dmsexplorer.settings.theme.ThemeColorGeneratorNormal;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
enum Theme {
    NORMAL(new ThemeParams.Builder()
            .setHtmlQuery("t=normal")
            .setThemeId(R.style.AppTheme)
            .setNoActionBarThemeId(R.style.AppTheme_NoActionBar)
            .setListThemeId(R.style.AppTheme_List)
            .setPopupThemeId(R.style.AppTheme_PopupOverlay)
            .setFullscreenThemeId(R.style.AppTheme_NoActionBar_FullScreen)
            .setPreferenceHeaderConverter(headers -> {
            })
            .setThemeColorGenerator(new ThemeColorGeneratorNormal())
            .setServerColorExtractor(new ServerColorExtractorNormal())
            .build()),
    DARK(new ThemeParams.Builder()
            .setHtmlQuery("t=dark")
            .setThemeId(R.style.DarkTheme)
            .setNoActionBarThemeId(R.style.DarkTheme_NoActionBar)
            .setListThemeId(R.style.DarkTheme_List)
            .setPopupThemeId(R.style.DarkTheme_PopupOverlay)
            .setFullscreenThemeId(R.style.DarkTheme_NoActionBar_FullScreen)
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
            case R.drawable.ic_play_settings:
                return R.drawable.ic_play_settings_white;
            case R.drawable.ic_function_settings:
                return R.drawable.ic_function_settings_white;
            case R.drawable.ic_view_settings:
                return R.drawable.ic_view_settings_white;
            case R.drawable.ic_expert_settings:
                return R.drawable.ic_expert_settings_white;
            case R.drawable.ic_info_settings:
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
