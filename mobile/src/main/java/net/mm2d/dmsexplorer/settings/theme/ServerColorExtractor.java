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

import net.mm2d.android.upnp.cds.MediaServer;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface ServerColorExtractor {
    void setServerThemeColor(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon);

    void setServerThemeColorAsync(
            @NonNull final MediaServer server,
            @Nullable final Bitmap icon);
}
