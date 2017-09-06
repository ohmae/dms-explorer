/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter;

import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;

import java.util.List;

import io.reactivex.Single;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ChapterFetcherFactory {
    private static final FetcherFactory[] sFetchers = new FetcherFactory[]{
            new SonyFetcherFactory(),
            new PanasonicFetcherFactory(),
    };

    @NonNull
    public static Single<List<Integer>> create(@NonNull final CdsObject object) {
        for (final FetcherFactory fetcher : sFetchers) {
            final Single<List<Integer>> single = fetcher.create(object);
            if (single != null) {
                return single;
            }
        }
        return Single.never();
    }
}
