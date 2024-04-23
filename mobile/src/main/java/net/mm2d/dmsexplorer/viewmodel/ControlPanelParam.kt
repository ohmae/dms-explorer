/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ControlPanelParam {
    private val bottomPaddingFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getBottomPaddingFlow(): Flow<Int> = bottomPaddingFlow
    fun setBottomPadding(padding: Int) {
        bottomPaddingFlow.value = padding
    }

    private val marginRightFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getMarginRightFlow(): Flow<Int> = marginRightFlow
    fun setMarginRight(margin: Int) {
        marginRightFlow.value = margin
    }

    private val backgroundColorFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun getBackgroundColorFlow(): Flow<Int> = backgroundColorFlow
    fun setBackgroundColor(color: Int) {
        backgroundColorFlow.value = color
    }
}
