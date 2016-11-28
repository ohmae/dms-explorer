/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class LaunchUtils {
    private static final String TAG = "LaunchUtils";
    public static void openUri(Context context, String uri) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        } catch (final ActivityNotFoundException e) {
            Log.w(TAG, e);
        }
    }
}
