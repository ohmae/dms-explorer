/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.adapter;

import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class RecyclerViewBindingAdapter {
    @BindingAdapter("itemDecoration")
    public static void addItemDecoration(@NonNull final RecyclerView view,
                                         @Nullable final ItemDecoration decor) {
        if (decor != null) {
            view.addItemDecoration(decor);
        }
    }
}
