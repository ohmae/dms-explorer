/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.model.adapter;

import android.databinding.BindingAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class SwipeRefreshLayoutBindingAdapter {
    @BindingAdapter("refreshing")
    public static void setRefreshing(SwipeRefreshLayout view, boolean refreshing) {
        view.setRefreshing(refreshing);
    }

    @BindingAdapter("colorSchemeRes")
    public static void setColorSchemeResources(SwipeRefreshLayout view, int[] colorResList) {
        view.setColorSchemeResources(colorResList);
    }

    @BindingAdapter("onRefreshListener")
    public static void setOnRefreshListener(SwipeRefreshLayout view, OnRefreshListener listener) {
        view.setOnRefreshListener(listener);
    }
}
