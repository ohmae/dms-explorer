/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.*
import android.graphics.Bitmap.Config
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import net.mm2d.android.util.DisplaySizeUtils
import net.mm2d.android.util.ViewUtils
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.databinding.IntroductoryOverlayBinding
import net.mm2d.dmsexplorer.util.ViewLayoutUtils
import java.util.concurrent.TimeUnit
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@SuppressLint("ViewConstructor")
class IntroductoryOverlay private constructor(
    private val activity: Activity,
    private val view: View,
    titleText: String,
    subtitleText: String,
    @ColorInt
    overlayColor: Int,
    @ColorInt
    private val dimmerColor: Int,
    private val timeout: Long,
) : RelativeLayout(activity, null, 0) {
    private val binding: IntroductoryOverlayBinding = IntroductoryOverlayBinding.inflate(activity.layoutInflater, this)
    private val circlePaint: Paint
    private val erasePaint: Paint
    private val removeTask = Runnable { this.dismiss() }
    private val margin: Int

    private var buffer: Bitmap? = null
    private var bufferCanvas: Canvas? = null
    private var centerY: Float = 0.toFloat()
    private var centerX: Float = 0.toFloat()
    private var holeRadius: Float = 0.toFloat()
    private var circleRadiusStart: Float = 0.toFloat()
    private var circleRadius: Float = 0.toFloat()
    private var animator: ValueAnimator? = null
    private var isVisible: Boolean = false

    init {
        if (titleText.isNotEmpty()) {
            binding.title.text = titleText
        }
        if (subtitleText.isNotEmpty()) {
            binding.subtitle.text = subtitleText
        }
        margin = activity.resources
            .getDimensionPixelSize(R.dimen.introduction_title_margin_horizontal)
        circlePaint = makeCirclePaint(overlayColor)
        erasePaint = makeErasePaint()
    }

    fun show() {
        if (isVisible) {
            return
        }
        isVisible = true
        adjustNavigation()
        ViewUtils.execAfterAllocateSize(view) {
            setUpDrawingParam()
            setUpAnimation()
            (activity.window.decorView as ViewGroup).addView(this)
            postDelayed(removeTask, timeout)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        adjustNavigation()
        ViewUtils.execOnLayout(view) {
            setUpDrawingParam()
            invalidate()
        }
    }

    private fun setUpAnimation() {
        animator = ValueAnimator.ofFloat(circleRadiusStart, circleRadius).also {
            it.duration = ANIMATION_DURATION
            it.interpolator = OvershootInterpolator()
            it.addUpdateListener { invalidate() }
            it.start()
        }
    }

    private fun adjustNavigation() {
        val size = DisplaySizeUtils.getNavigationBarArea(activity)
        ViewLayoutUtils.setLayoutMarginRight(binding.title, margin + size.x)
    }

    private fun setUpDrawingParam() {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        centerX = rect.centerX().toFloat()
        centerY = rect.centerY().toFloat()
        holeRadius = calcHoleRadius(rect)
        circleRadius = calcCircleRadius(activity)
        circleRadiusStart = calcCircleRadiusStart(activity)
    }

    private fun dismiss() {
        removeCallbacks(removeTask)
        if (!isVisible) {
            return
        }
        isVisible = false
        animate().alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dismissInner()
                }
            }).start()
    }

    private fun dismissInner() {
        if (context != null) {
            (activity.window.decorView as ViewGroup).removeView(this)
        }
        recycleBuffer()
    }

    private fun recycleBuffer() {
        buffer?.recycle()
        buffer = null
        bufferCanvas = null
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width = width
        val height = height
        val animator = animator
        if (width <= 0 || height <= 0 || animator == null) {
            super.dispatchDraw(canvas)
            return
        }
        allocateBuffer(width, height)
        val buffer = buffer!!
        val bufferCanvas = bufferCanvas!!
        val radius = animator.animatedValue as Float
        bufferCanvas.drawColor(dimmerColor, PorterDuff.Mode.SRC)
        bufferCanvas.drawCircle(centerX, centerY, radius, circlePaint)
        bufferCanvas.drawCircle(centerX, centerY, holeRadius, erasePaint)
        canvas.drawBitmap(buffer, 0f, 0f, null)
        super.dispatchDraw(canvas)
    }

    private fun allocateBuffer(width: Int, height: Int) {
        val buffer = buffer
        if (buffer == null || buffer.width != width || buffer.height != height) {
            recycleBuffer()
            this.buffer = Bitmap.createBitmap(width, height, Config.ARGB_8888).also {
                this.bufferCanvas = Canvas(it)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (animator?.isRunning == false && event.action == MotionEvent.ACTION_UP) {
            dismiss()
        }
        return true
    }

    class Builder(
        val activity: Activity,
    ) {
        private var view: View? = null
        private var titleText: String? = null
        private var subtitleText: String? = null

        @ColorInt
        private var overlayColor: Int = 0

        @ColorInt
        private var dimmerColor: Int = 0
        private var timeout: Long = 0

        fun build(): IntroductoryOverlay {
            val view = view
                ?: throw IllegalStateException("View can't be null")
            val titleText = titleText
                ?: throw IllegalStateException("TitleText can't be null")
            val subtitleText = subtitleText
                ?: throw IllegalStateException("SubtitleText can't be null")
            return IntroductoryOverlay(
                activity,
                view,
                titleText,
                subtitleText,
                overlayColor,
                dimmerColor,
                if (timeout < ANIMATION_DURATION) DEFAULT_TIMEOUT else timeout,
            )
        }

        fun setView(view: View): Builder = apply {
            this.view = view
        }

        fun setTimeout(timeout: Long): Builder = apply {
            this.timeout = timeout
        }

        fun setOverlayColor(@ColorRes colorId: Int): Builder = apply {
            overlayColor = ContextCompat.getColor(activity, colorId)
        }

        fun setDimmerColor(@ColorRes colorId: Int): Builder = apply {
            dimmerColor = ContextCompat.getColor(activity, colorId)
        }

        fun setTitleText(@StringRes stringId: Int): Builder = apply {
            titleText = activity.getString(stringId)
        }

        fun setTitleText(text: String): Builder = apply {
            titleText = text
        }

        fun setSubtitleText(@StringRes stringId: Int): Builder = apply {
            subtitleText = activity.getString(stringId)
        }

        fun setSubtitleText(text: String): Builder = apply {
            subtitleText = text
        }
    }

    companion object {
        private const val ANIMATION_DURATION: Long = 400
        private val DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(5)

        private fun makeCirclePaint(@ColorInt color: Int): Paint {
            return Paint().also {
                it.color = color
                it.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                it.isAntiAlias = true
            }
        }

        private fun makeErasePaint(): Paint = Paint().also {
            it.color = Color.BLACK
            it.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            it.isAntiAlias = true
        }

        private fun calcHoleRadius(viewRect: Rect): Float {
            val w = viewRect.width().toDouble()
            val h = viewRect.height().toDouble()
            return hypot(w, h).toFloat() / 2f
        }

        private fun calcCircleRadius(activity: Activity): Float {
            val size = DisplaySizeUtils.getRealSize(activity)
            val x = size.x
            val y = size.y
            return min(x, y).toFloat()
        }

        private fun calcCircleRadiusStart(activity: Activity): Float {
            val size = DisplaySizeUtils.getRealSize(activity)
            val x = size.x
            val y = size.y
            return sqrt((x * x + y * y).toDouble()).toFloat()
        }
    }
}
