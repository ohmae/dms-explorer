/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.model;

import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PropertyItemModel {
    public final CharSequence title;
    public final CharSequence description;

    public PropertyItemModel(@NonNull CharSequence title, @NonNull CharSequence description) {
        this.title = title;
        this.description = description;
    }
}
