/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import androidx.annotation.ColorInt
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ControlPanelParam : BaseObservable() {
    @get:Bindable
    var bottomPadding: Int = 0
        set(padding) {
            field = padding
            notifyPropertyChanged(BR.bottomPadding)
        }
    @get:Bindable
    var marginRight: Int = 0
        set(margin) {
            field = margin
            notifyPropertyChanged(BR.marginRight)
        }
    @get:Bindable
    var backgroundColor: Int = 0
        set(@ColorInt color) {
            field = color
            notifyPropertyChanged(BR.backgroundColor)
        }
}
