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

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class ServerColorExtractorDefault implements ServerColorExtractor {
    private static final ThemeColorGenerator GENERATOR = new ThemeColorGeneratorDefault();

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
        new Palette.Builder(icon)
                .generate(palette -> extractFromPalette(server, palette));
    }

    private void extract(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon) {
        final Palette palette = icon != null
                ? new Palette.Builder(icon).generate()
                : null;
        extractFromPalette(server, palette);
    }

    private void extractFromPalette(
            @NonNull final MediaServer server,
            @Nullable final Palette palette) {
        final String friendlyName = server.getFriendlyName();
        int expandedColor = GENERATOR.getExpandedToolbarColor(friendlyName);
        int collapsedColor = GENERATOR.getControlColor(friendlyName);
        if (palette != null) {
            final Swatch lightSwatch = PaletteUtils.selectLightSwatch(palette);
            if (lightSwatch != null) {
                expandedColor = lightSwatch.getRgb();
            }
            final Swatch darkSwatch = PaletteUtils.selectDarkSwatch(palette);
            if (darkSwatch != null) {
                collapsedColor = darkSwatch.getRgb();
            }
        }
        server.putIntTag(Const.KEY_TOOLBAR_EXPANDED_COLOR, expandedColor);
        server.putIntTag(Const.KEY_TOOLBAR_COLLAPSED_COLOR, collapsedColor);
        server.putBooleanTag(Const.KEY_HAS_TOOLBAR_COLOR, true);
    }
}
