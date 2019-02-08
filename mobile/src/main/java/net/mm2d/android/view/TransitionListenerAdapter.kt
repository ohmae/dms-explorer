/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.view

import android.os.Build
import android.transition.Transition

import androidx.annotation.RequiresApi

/**
 * Transition.TransitionListenerの空実装。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
open class TransitionListenerAdapter : Transition.TransitionListener {
    override fun onTransitionStart(transition: Transition) {}
    override fun onTransitionEnd(transition: Transition) {}
    override fun onTransitionCancel(transition: Transition) {}
    override fun onTransitionPause(transition: Transition) {}
    override fun onTransitionResume(transition: Transition) {}
}
