/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer

import net.mm2d.dmsexplorer.domain.model.ControlPointModel
import net.mm2d.dmsexplorer.domain.model.MediaRendererModel
import net.mm2d.dmsexplorer.domain.model.MediaServerModel
import net.mm2d.dmsexplorer.domain.model.OpenUriModel
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel
import net.mm2d.dmsexplorer.domain.model.ThemeModel

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class Repository {
    abstract val themeModel: ThemeModel
    abstract val openUriModel: OpenUriModel
    abstract val controlPointModel: ControlPointModel
    abstract val mediaServerModel: MediaServerModel?
    abstract val mediaRendererModel: MediaRendererModel?
    abstract val playbackTargetModel: PlaybackTargetModel?

    companion object {
        private lateinit var instance: Repository

        fun get(): Repository = instance

        internal fun set(
            instance: Repository,
        ) {
            this.instance = instance
        }
    }
}
