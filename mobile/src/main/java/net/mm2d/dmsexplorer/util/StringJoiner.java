/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class StringJoiner {
    private final StringBuilder mBuilder = new StringBuilder();

    public void join(@Nullable final String string) {
        join(string, '\n');
    }

    public void join(
            @Nullable final String string,
            final char delimiter) {
        if (TextUtils.isEmpty(string)) {
            return;
        }
        if (mBuilder.length() != 0) {
            mBuilder.append(delimiter);
        }
        mBuilder.append(string);
    }

    public void join(
            @Nullable final String string,
            @NonNull final String delimiter) {
        if (TextUtils.isEmpty(string)) {
            return;
        }
        if (mBuilder.length() != 0) {
            mBuilder.append(delimiter);
        }
        mBuilder.append(string);
    }

    public int length() {
        return mBuilder.length();
    }

    @Override
    public String toString() {
        return mBuilder.toString();
    }
}
