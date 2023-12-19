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
interface MovieActivityPipHelper {
    fun register()
    fun unregister()
    fun setControlPanelModel(model: ControlPanelModel?)
    fun enterPictureInPictureMode(contentView: View)
}
