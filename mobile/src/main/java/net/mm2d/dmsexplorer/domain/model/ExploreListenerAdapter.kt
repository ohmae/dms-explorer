/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import net.mm2d.dmsexplorer.domain.entity.ContentEntity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ExploreListenerAdapter : ExploreListener {
    override fun onStart() = Unit
    override fun onUpdate(
        list: List<ContentEntity>,
    ) = Unit
    override fun onComplete() = Unit
}
