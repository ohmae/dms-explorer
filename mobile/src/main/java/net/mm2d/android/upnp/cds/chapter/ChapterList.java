/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter;

import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ChapterList {
    public interface Callback {
        void onResult(@NonNull List<Integer> result);
    }

    private static final Fetcher[] sFetchers = new Fetcher[]{
            new SonyFetcher(),
            new PanasonicFetcher(),
    };

    public static void get(
            @NonNull final CdsObject object,
            @NonNull final Callback callback) {
        for (final Fetcher fetcher : sFetchers) {
            if (fetcher.get(object, callback)) {
                return;
            }
        }
        callback.onResult(Collections.emptyList());
    }
}
