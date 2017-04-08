/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.SeekBar;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class SeekBarBindingAdapter {
    @BindingAdapter("progressDrawable")
    public static void setProgressDrawable(@NonNull final SeekBar view,
                                           @Nullable final Drawable drawable) {
        if (drawable != null) {
            view.setProgressDrawable(drawable);
        }
    }
}
