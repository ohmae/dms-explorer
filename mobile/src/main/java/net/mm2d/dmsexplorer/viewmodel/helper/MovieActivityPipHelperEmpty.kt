/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel.helper

import android.view.View
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class MovieActivityPipHelperEmpty : MovieActivityPipHelper {
    override fun register() = Unit
    override fun unregister() = Unit
    override fun setControlPanelModel(
        model: ControlPanelModel?,
    ) = Unit

    override fun enterPictureInPictureMode(
        contentView: View,
    ) = Unit
}
