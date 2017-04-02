/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter.property;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class LinkFormatter implements PropertyFormatter {
    @NonNull
    @Override
    public CharSequence format(@NonNull final Context context, @NonNull final String string) {
        final SpannableString ss = new SpannableString(string);
        ss.setSpan(new LinkSpan(context, string), 0, string.length(), Spanned.SPAN_MARK_POINT);
        return ss;
    }
}
