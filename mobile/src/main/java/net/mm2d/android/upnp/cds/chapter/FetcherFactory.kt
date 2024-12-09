/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds.chapter

import io.reactivex.Single
import net.mm2d.android.upnp.cds.CdsObject

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal interface FetcherFactory {
    fun create(
        cdsObject: CdsObject,
    ): Single<List<Int>>?
}
