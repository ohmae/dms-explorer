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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import net.mm2d.dmsexplorer.R
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class FullscreenHelper(
    private val rootView: View,
    private val topView: View? = null,
    private val bottomView: View? = null
) {
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
        rootView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (!posted && !isInPictureInPictureMode && visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0) {
                showNavigation()
            }
        }
    }

    private fun postHideNavigation(interval: Long) {
        handler.removeCallbacks(hideNavigationTask)
        handler.postDelayed(hideNavigationTask, interval)
        posted = true
    }

    fun showNavigation(interval: Long = NAVIGATION_INTERVAL): Boolean {
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
        rootView.systemUiVisibility = SYSTEM_UI_VISIBLE
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
        rootView.systemUiVisibility = SYSTEM_UI_INVISIBLE
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
        rootView.systemUiVisibility = SYSTEM_UI_INVISIBLE
        handler.removeCallbacks(hideNavigationTask)
    }

    fun onPictureInPictureModeChanged(pip: Boolean) {
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
        private const val SYSTEM_UI_VISIBLE: Int = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        private const val SYSTEM_UI_INVISIBLE: Int = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

    }
}
