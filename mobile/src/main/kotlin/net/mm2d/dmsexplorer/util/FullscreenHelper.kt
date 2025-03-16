/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import net.mm2d.dmsexplorer.R
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class FullscreenHelper(
    window: Window,
    rootView: View,
    private val topView: View? = null,
    private val bottomView: View? = null,
) {
    private val controller: WindowInsetsControllerCompat = WindowInsetsControllerCompat(window, rootView)
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val enterFromTop: Animation
    private val enterFromBottom: Animation
    private val exitToTop: Animation
    private val exitToBottom: Animation
    private val hideNavigationTask = Runnable { this.hideNavigation() }
    private var posted: Boolean = false
    private var isInPictureInPictureMode: Boolean = false

    init {
        val context = rootView.context
        enterFromTop = AnimationUtils.loadAnimation(context, R.anim.enter_from_top)
        enterFromBottom = AnimationUtils.loadAnimation(context, R.anim.enter_from_bottom)
        exitToTop = AnimationUtils.loadAnimation(context, R.anim.exit_to_top)
        exitToBottom = AnimationUtils.loadAnimation(context, R.anim.exit_to_bottom)
    }

    private fun postHideNavigation(
        interval: Long,
    ) {
        handler.removeCallbacks(hideNavigationTask)
        handler.postDelayed(hideNavigationTask, interval)
        posted = true
    }

    fun showNavigation(
        interval: Long = NAVIGATION_INTERVAL,
    ): Boolean {
        if (isInPictureInPictureMode) {
            return false
        }
        var execute = false
        topView?.let {
            if (it.visibility != View.VISIBLE) {
                it.clearAnimation()
                it.startAnimation(enterFromTop)
                it.visibility = View.VISIBLE
                execute = true
            }
        }
        bottomView?.let {
            if (it.visibility != View.VISIBLE) {
                it.clearAnimation()
                it.startAnimation(enterFromBottom)
                it.visibility = View.VISIBLE
                execute = true
            }
        }
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        controller.show(WindowInsetsCompat.Type.systemBars())
        postHideNavigation(interval)
        return execute
    }

    private fun hideNavigation() {
        posted = false
        topView?.let {
            if (it.visibility != View.GONE) {
                it.clearAnimation()
                it.startAnimation(exitToTop)
                it.visibility = View.GONE
            }
        }
        bottomView?.let {
            if (it.visibility != View.GONE) {
                it.clearAnimation()
                it.startAnimation(exitToBottom)
                it.visibility = View.GONE
            }
        }
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        handler.removeCallbacks(hideNavigationTask)
    }

    fun hideNavigationImmediately() {
        posted = false
        topView?.let {
            it.clearAnimation()
            it.visibility = View.GONE
        }
        bottomView?.let {
            it.clearAnimation()
            it.visibility = View.GONE
        }
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        handler.removeCallbacks(hideNavigationTask)
    }

    fun onPictureInPictureModeChanged(
        pip: Boolean,
    ) {
        isInPictureInPictureMode = pip
        if (pip) {
            handler.removeCallbacks(hideNavigationTask)
            topView?.let {
                it.clearAnimation()
                it.visibility = View.GONE
            }
            bottomView?.let {
                it.clearAnimation()
                it.visibility = View.GONE
            }
        } else {
            postHideNavigation(NAVIGATION_INTERVAL)
        }
    }

    fun terminate() {
        handler.removeCallbacks(hideNavigationTask)
    }

    companion object {
        private val NAVIGATION_INTERVAL = TimeUnit.SECONDS.toMillis(3)
    }
}
