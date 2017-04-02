/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.adapter.property;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class TextFormatter implements PropertyFormatter {
    @NonNull
    @Override
    public CharSequence format(@NonNull final Context context, @NonNull final String string) {
        return string;
    }
}
