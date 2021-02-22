/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class NestedScrollingWebView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild {
    private val helper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    private val touchSlop: Int
    private val buffer = IntArray(2)
    private var startY: Float = 0.toFloat()
    private var prevY: Float = 0.toFloat()
    private var scrolling: Boolean = false
    private var appBarLayout: AppBarLayout? = null
    private var behavior: Behavior? = null

    init {
        isNestedScrollingEnabled = true
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        setBackgroundColor(0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        behavior =
            (layoutParams as? androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams)?.behavior as? Behavior
        val p = parent as? ViewGroup ?: return
        for (i in 0 until p.childCount) {
            val child = p.getChildAt(i)
            if (child is AppBarLayout) {
                appBarLayout = child
                return
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> startY = event.rawY
            MotionEvent.ACTION_MOVE -> onTouchMove(event)
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> onTouchEnd()
        }
        prevY = event.rawY
        return super.onTouchEvent(event)
    }

    private fun onTouchMove(event: MotionEvent) {
        if (event.pointerCount != 1) {
            return
        }
        if (scrolling) {
            val dy = (prevY - event.rawY).toInt()
            val consumed: IntArray = buffer
            dispatchNestedPreScroll(0, dy, consumed, null)
            dispatchNestedScroll(0, consumed[1], 0, dy - consumed[1], null)
            return
        }
        if (abs(startY - event.rawY) > touchSlop) {
            scrolling = true
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
        }
    }

    private fun onTouchEnd() {
        if (scrolling) {
            stopNestedScroll()
            scrolling = false
        }
        val appBar = appBarLayout ?: return
        appBar.setExpanded(appBar.bottom > appBar.height / 2)
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        helper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean = helper.isNestedScrollingEnabled

    override fun startNestedScroll(axes: Int): Boolean {
        behavior?.scrollByUser = true
        return helper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        behavior?.scrollByUser = false
        helper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean = helper.hasNestedScrollingParent()

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean = helper.dispatchNestedScroll(
        dxConsumed,
        dyConsumed,
        dxUnconsumed,
        dyUnconsumed,
        offsetInWindow
    )

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean = helper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean = helper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean =
        helper.dispatchNestedPreFling(velocityX, velocityY)

    class Behavior(
        context: Context,
        attrs: AttributeSet?
    ) : AppBarLayout.ScrollingViewBehavior(context, attrs) {
        var scrollByUser = true
        private var prevBottom = 0

        override fun onDependentViewChanged(
            parent: androidx.coordinatorlayout.widget.CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            if (!scrollByUser) {
                val dy = dependency.bottom - prevBottom
                child.scrollBy(0, dy)
            }
            prevBottom = dependency.bottom
            return super.onDependentViewChanged(parent, child, dependency)
        }
    }
}
