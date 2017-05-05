/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View.OnClickListener;

import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter.Type;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PropertyItemModel {
    @NonNull
    public final CharSequence title;
    @NonNull
    public final CharSequence description;
    public final OnClickListener onClickListener;
    public final boolean isLink;
    public final boolean enableDescription;

    public PropertyItemModel(@NonNull final String title,
                             @NonNull final Type type,
                             @NonNull final String description,
                             @Nullable final OnClickListener listener) {
        isLink = type == Type.LINK;
        enableDescription = (type != Type.TITLE && !TextUtils.isEmpty(description));
        onClickListener = isLink ? listener : null;
        this.title = title;
        this.description = description;
    }
}
