/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class StatusListenerAdapter : StatusListener {
    override fun notifyDuration(
        duration: Int,
    ) = Unit

    override fun notifyProgress(
        progress: Int,
    ) = Unit

    override fun notifyPlayingState(
        playing: Boolean,
    ) = Unit

    override fun notifyChapterList(
        chapterList: List<Int>,
    ) = Unit

    override fun onError(
        what: Int,
        extra: Int,
    ): Boolean = false

    override fun onInfo(
        what: Int,
        extra: Int,
    ): Boolean = false
    override fun onCompletion() = Unit
}
