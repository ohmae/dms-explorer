/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter;

import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class FloatingActionButtonBindingAdapter {
    @BindingAdapter("show")
    public static void setShow(@NonNull final FloatingActionButton view, final boolean show) {
        if (show) {
            view.show();
        } else {
            view.hide();
        }
    }

    @BindingAdapter("backgroundTint")
    public static void setBackgroundTint(@NonNull final FloatingActionButton view, @ColorInt final int color) {
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
