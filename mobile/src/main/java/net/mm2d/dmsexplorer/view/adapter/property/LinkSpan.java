/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter.property;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.style.ClickableSpan;
import android.view.View;

import net.mm2d.android.util.LaunchUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class LinkSpan extends ClickableSpan {
    private final Context mContext;
    private final String mUri;

    LinkSpan(@NonNull final Context context, @NonNull final String uri) {
        mContext = context;
        mUri = uri;
    }

    @Override
    public void onClick(@NonNull final View widget) {
        LaunchUtils.openUri(mContext, mUri);
    }
}
