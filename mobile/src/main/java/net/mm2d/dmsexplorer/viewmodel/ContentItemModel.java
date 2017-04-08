/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DrawableUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.util.ThemeUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ContentItemModel {
    public final boolean selected;
    @NonNull
    public final Drawable accentBackground;
    @NonNull
    public final String accentText;
    @NonNull
    public final String title;
    @NonNull
    public final String description;
    public final int imageResource;

    public ContentItemModel(@NonNull final Context context,
                            @NonNull final CdsObject object,
                            final boolean selected) {
        this.selected = selected;
        final String name = object.getTitle();
        accentText = TextUtils.isEmpty(name) ? ""
                : AribUtils.toDisplayableString(name.substring(0, 1));
        accentBackground = DrawableUtils.get(context, R.drawable.ic_circle);
        DrawableCompat.setTint(accentBackground, ThemeUtils.getAccentColor(name));
        title = AribUtils.toDisplayableString(name);
        description = object.getUpnpClass();
        imageResource = getImageResource(object.getType());
    }

    private static int getImageResource(int type) {
        switch (type) {
            case CdsObject.TYPE_CONTAINER:
                return R.drawable.ic_folder;
            case CdsObject.TYPE_AUDIO:
                return R.drawable.ic_music;
            case CdsObject.TYPE_IMAGE:
                return R.drawable.ic_image;
            case CdsObject.TYPE_VIDEO:
                return R.drawable.ic_movie;
            case CdsObject.TYPE_UNKNOWN:
            default:
                return 0;
        }
    }
}
