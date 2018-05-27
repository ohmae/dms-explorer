/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DrawableUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.settings.theme.ThemeColorGenerator;

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
    public final boolean hasDescription;
    @DrawableRes
    public final int imageResource;
    public final boolean isProtected;

    public ContentItemModel(
            @NonNull final Context context,
            @NonNull final ContentEntity entity,
            final boolean selected) {
        this.selected = selected;
        final String name = entity.getName();
        accentText = TextUtils.isEmpty(name) ? ""
                : AribUtils.toDisplayableString(name.substring(0, 1));
        accentBackground = DrawableUtils.get(context, R.drawable.ic_circle);
        accentBackground.mutate();
        final ThemeColorGenerator generator = Settings.get()
                .getThemeParams()
                .getThemeColorGenerator();
        DrawableCompat.setTint(accentBackground, generator.getIconColor(name));
        title = AribUtils.toDisplayableString(name);
        description = entity.getDescription();
        hasDescription = !TextUtils.isEmpty(description);
        imageResource = getImageResource(entity);
        isProtected = entity.isProtected();
    }

    @DrawableRes
    private static int getImageResource(@NonNull final ContentEntity entity) {
        switch (entity.getType()) {
            case CONTAINER:
                return R.drawable.ic_folder;
            case MOVIE:
                return R.drawable.ic_movie;
            case MUSIC:
                return R.drawable.ic_music;
            case PHOTO:
                return R.drawable.ic_image;
            default:
                return 0;
        }
    }
}
