/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PropertyItemModel {
    @NonNull
    public final CharSequence title;
    @NonNull
    public final CharSequence description;

    public PropertyItemModel(@NonNull final CharSequence title,
                             @NonNull final CharSequence description) {
        this.title = title;
        this.description = description;
    }
}
