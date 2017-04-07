/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter;

import android.databinding.BindingAdapter;
import android.view.View;

import net.mm2d.dmsexplorer.util.ViewLayoutUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewBindingAdapter {
    @BindingAdapter("layout_marginRight")
    public static void setLayoutMarginRight(View view, int rightMargin) {
        ViewLayoutUtils.setLayoutMarginRight(view, rightMargin);
    }

    @BindingAdapter("layout_height")
    public static void setLayoutHeight(View view, int height) {
        ViewLayoutUtils.setLayoutHeight(view, height);
    }
}
