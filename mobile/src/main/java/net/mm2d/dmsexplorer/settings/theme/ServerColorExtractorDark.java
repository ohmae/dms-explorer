/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.settings.theme;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.util.ColorUtils;
import net.mm2d.dmsexplorer.util.PaletteUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerColorExtractorDark implements ServerColorExtractor {
    private static final ThemeColorGenerator GENERATOR = new ThemeColorGeneratorDark();

    @Override
    public void invoke(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon) {
        if (server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            return;
        }
        extract(server, icon);
    }

    @Override
    public void invokeAsync(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon) {
        if (server.getBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, false)) {
            return;
        }
        if (icon == null) {
            extract(server, null);
            return;
        }
        new Palette.Builder(icon).generate(palette -> extractFromPalette(server, palette));
    }

    private void extract(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon) {
        final Palette palette = icon == null ? null : new Palette.Builder(icon).generate();
        extractFromPalette(server, palette);
    }

    private void extractFromPalette(
            @NonNull final MediaServer server,
            @Nullable final Palette palette) {
        final String friendlyName = server.getFriendlyName();
        int expandedColor = GENERATOR.getPastelColor(friendlyName);
        int collapsedColor = GENERATOR.getDeepColor(friendlyName);
        if (palette != null) {
            final Swatch lightSwatch = PaletteUtils.selectLightSwatch(palette);
            if (lightSwatch != null) {
                expandedColor = ColorUtils.getDarkerColor(lightSwatch.getRgb(), 0.3f);
            }
            final Swatch darkSwatch = PaletteUtils.selectDarkSwatch(palette);
            if (darkSwatch != null) {
                collapsedColor = ColorUtils.getDarkerColor(darkSwatch.getRgb(), 0.3f);
            }
        }
        server.putIntTag(Const.KEY_TOOLBAR_EXPANDED_COLOR, expandedColor);
        server.putIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, collapsedColor);
        server.putBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, true);
    }
}
