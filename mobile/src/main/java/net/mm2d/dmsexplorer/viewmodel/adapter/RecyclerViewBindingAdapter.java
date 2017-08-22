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
import android.view.View;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class RecyclerViewBindingAdapter {
    @BindingAdapter("itemDecoration")
    public static void addItemDecoration(
            @NonNull final RecyclerView view,
            @Nullable final ItemDecoration decor) {
        if (decor != null) {
            view.addItemDecoration(decor);
        }
    }

    // 選択項目を中央に表示させる処理
    // FIXME: DataBindingを使ったことで返って複雑化してしまっている
    @BindingAdapter("scrollPosition")
    public static void setScrollPosition(
            @NonNull final RecyclerView view,
            final int position) {
        if (position < 0) {
            return;
        }
        view.scrollToPosition(position);
        view.post(() -> scrollToCenter(view, position));
    }

    public static void scrollToCenter(
            @NonNull final RecyclerView view,
            final int position) {
        final View child = findPositionView(view, position);
        if (child == null) {
            return;
        }
        final int dy = (int) (child.getTop() - (view.getHeight() - child.getHeight()) / 2f);
        view.smoothScrollBy(0, dy);
    }

    @Nullable
    private static View findPositionView(
            @NonNull final RecyclerView view,
            final int position) {
        final View child = view.getChildAt(0);
        if (child == null) {
            return null;
        }
        final int top = view.getChildAdapterPosition(child);
        return top < 0 ? null : view.getChildAt(position - top);
    }
}
