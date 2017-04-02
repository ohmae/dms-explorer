/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.util.ThemeUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CdsItemModel {
    public final int markVisibility;
    public final float translationZ;
    public final GradientDrawable accentBackground;
    public final String accentText;
    public final String title;
    public final String description;
    public final int imageResource;

    public CdsItemModel(Context context, CdsObject object, boolean selected) {
        markVisibility = selected ? View.VISIBLE : View.INVISIBLE;
        final Resources res = context.getResources();
        translationZ = selected ? res.getDimension(R.dimen.list_item_focus_elevation) : 0;
        final String name = object.getTitle();
        accentText = TextUtils.isEmpty(name) ? ""
                : AribUtils.toDisplayableString(name.substring(0, 1));
        accentBackground = new GradientDrawable();
        accentBackground.setCornerRadius(res.getDimension(R.dimen.accent_radius));
        accentBackground.setColor(ThemeUtils.getAccentColor(name));
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
