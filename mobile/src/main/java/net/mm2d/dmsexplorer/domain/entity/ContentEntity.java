/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public interface ContentEntity {
    @NonNull
    Object getObject();

    @NonNull
    String getName();

    @NonNull
    String getDescription();

    @NonNull
    Uri getArtUri();

    @NonNull
    Uri getIconUri();

    @NonNull
    ContentType getType();

    @Nullable
    Uri getUri();

    @Nullable
    String getMimeType();

    int getResourceCount();

    boolean hasResource();

    boolean isProtected();

    void selectResource(int index);
}
