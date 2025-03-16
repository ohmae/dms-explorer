/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.view

import android.transition.Transition

/**
 * Transition.TransitionListenerの空実装。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
open class TransitionListenerAdapter : Transition.TransitionListener {
    override fun onTransitionStart(
        transition: Transition,
    ) = Unit

    override fun onTransitionEnd(
        transition: Transition,
    ) = Unit

    override fun onTransitionCancel(
        transition: Transition,
    ) = Unit

    override fun onTransitionPause(
        transition: Transition,
    ) = Unit

    override fun onTransitionResume(
        transition: Transition,
    ) = Unit
}
