/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewLayoutUtils {
    private ViewLayoutUtils() {
        throw new AssertionError();
    }

    public static void setLayoutMarginRight(
            @NonNull final View view,
            @Px final int margin) {
        final LayoutParams params = view.getLayoutParams();
        if (!(params instanceof MarginLayoutParams)) {
            return;
        }
        final MarginLayoutParams marginParams = (MarginLayoutParams) params;
        marginParams.rightMargin = margin;
        view.setLayoutParams(params);
    }

    public static void setPaddingBottom(
            @NonNull final View view,
            @Px final int padding) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), padding);
    }
}
