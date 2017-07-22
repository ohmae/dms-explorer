/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter;

import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.chapter.ChapterList.Callback;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
interface Fetcher {
    boolean get(@NonNull CdsObject object, @NonNull Callback callback);
}
