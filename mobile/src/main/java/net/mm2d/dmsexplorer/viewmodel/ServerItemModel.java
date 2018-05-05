/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DrawableUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.settings.ColorThemeParams;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.ServerColorExtractor;
import net.mm2d.dmsexplorer.util.ThemeColorGenerator;
import net.mm2d.upnp.Icon;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ServerItemModel {
    public final boolean selected;
    @Nullable
    public final Drawable accentBackground;
    @Nullable
    public final String accentText;
    @Nullable
    public final Bitmap accentIcon;
    @NonNull
    public final String title;
    @NonNull
    public final String description;

    public ServerItemModel(
            @NonNull final Context context,
            @NonNull final MediaServer server,
            final boolean selected) {
        this.selected = selected;
        final String name = server.getFriendlyName();
        final Icon icon = server.getIcon();
        title = name;
        description = makeDescription(server);
        final ColorThemeParams params = new Settings(context).getColorThemeParams();
        final ThemeColorGenerator generator = params.getThemeColorGenerator();
        final ServerColorExtractor extractor = params.getServerColorExtractor();
        if (icon == null) {
            accentIcon = null;
            accentText = TextUtils.isEmpty(name) ? ""
                    : AribUtils.toDisplayableString(name.substring(0, 1));
            accentBackground = DrawableUtils.get(context, R.drawable.ic_circle);
            accentBackground.mutate();
            DrawableCompat.setTint(accentBackground, generator.getIconColor(name));
            extractor.setServerThemeColorAsync(server, null);
            return;
        }
        final byte[] binary = icon.getBinary();
        accentIcon = BitmapFactory.decodeByteArray(binary, 0, binary.length);
        accentText = null;
        accentBackground = null;
        extractor.setServerThemeColorAsync(server, accentIcon);
    }

    private String makeDescription(@NonNull final MediaServer server) {
        final StringBuilder sb = new StringBuilder();
        final String manufacture = server.getManufacture();
        if (manufacture != null && !manufacture.isEmpty()) {
            sb.append(manufacture);
            sb.append('\n');
        }
        sb.append("IP: ");
        sb.append(server.getIpAddress());
        final String serial = server.getSerialNumber();
        if (serial != null && !serial.isEmpty()) {
            sb.append("  ");
            sb.append("Serial: ");
            sb.append(serial);
        }
        return sb.toString();
    }
}
