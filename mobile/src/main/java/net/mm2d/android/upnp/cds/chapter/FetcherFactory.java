/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.CdsObject;

import java.util.List;

import io.reactivex.Single;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
interface FetcherFactory {
    @Nullable
    Single<List<Integer>> create(@NonNull CdsObject object);
}
