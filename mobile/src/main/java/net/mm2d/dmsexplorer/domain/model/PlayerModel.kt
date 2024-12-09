/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.net.Uri
import net.mm2d.dmsexplorer.domain.entity.ContentEntity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
interface PlayerModel {
    interface StatusListener {
        fun notifyDuration(
            duration: Int,
        )

        fun notifyProgress(
            progress: Int,
        )

        fun notifyPlayingState(
            playing: Boolean,
        )

        fun notifyChapterList(
            chapterList: List<Int>,
        )

        fun onError(
            what: Int,
            extra: Int,
        ): Boolean

        fun onInfo(
            what: Int,
            extra: Int,
        ): Boolean
        fun onCompletion()
    }

    val name: String
    val progress: Int
    val duration: Int
    val isPlaying: Boolean
    fun canPause(): Boolean
    fun terminate()
    fun setStatusListener(
        listener: StatusListener,
    )

    fun setUri(
        uri: Uri,
        entity: ContentEntity?,
    )

    fun restoreSaveProgress(
        progress: Int,
    )
    fun play()
    fun pause()
    fun seekTo(
        position: Int,
    )
    fun next(): Boolean
    fun previous(): Boolean
}
