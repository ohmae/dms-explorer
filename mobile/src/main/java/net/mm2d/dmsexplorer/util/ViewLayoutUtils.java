/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewLayoutUtils {

    public static void setLayoutHeight(final @NonNull View view, int height) {
        final LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    public static void setLayoutMarginRight(View view, int rightMargin) {
        final LayoutParams params = view.getLayoutParams();
        if (!(params instanceof MarginLayoutParams)) {
            return;
        }
        final MarginLayoutParams marginParams = (MarginLayoutParams) params;
        marginParams.rightMargin = rightMargin;
        view.setLayoutParams(params);
    }
}
