/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PipHelpers {
    private static final MovieActivityPipHelper MOVIE_MOCK = new MovieActivityPipHelperEmpty();

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @NonNull
    public static MovieActivityPipHelper getMovieHelper(@NonNull final Activity activity) {
        if (isSupported()) {
            return new MovieActivityPipHelperOreo(activity);
        }
        return MOVIE_MOCK;
    }
}
