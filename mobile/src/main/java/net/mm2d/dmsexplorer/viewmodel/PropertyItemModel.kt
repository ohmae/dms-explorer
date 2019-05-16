/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import android.view.View.OnClickListener
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter.Type

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class PropertyItemModel(
    val title: CharSequence,
    type: Type,
    val description: CharSequence,
    listener: OnClickListener?
) {
    val isLink: Boolean = type == Type.LINK
    val enableDescription: Boolean = type !== Type.TITLE && description.isNotEmpty()
    val onClickListener: OnClickListener? = if (isLink) listener else null
}
