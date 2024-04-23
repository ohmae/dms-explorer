/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntDef
import kotlin.math.abs

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ScrubBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val paint: Paint
    private var scrubBarListener = LISTENER

    @Dimension(unit = Dimension.PX)
    private val trackWidth: Float

    @Dimension(unit = Dimension.PX)
    private val trackWidthHalf: Float

    @Dimension(unit = Dimension.PX)
    private val smallThumbRadius: Float

    @Dimension(unit = Dimension.PX)
    private val largeThumbRadius: Float

    @Dimension(unit = Dimension.PX)
    private var scrubUnitLength: Int = 0

    @ColorInt
    private var enabledProgressColor: Int = 0

    @ColorInt
    private var enabledTrackColor = DEFAULT_ENABLED_TRACK_COLOR

    @ColorInt
    private var enabledSectionColor = DEFAULT_ENABLED_SECTION_COLOR

    @ColorInt
    private var disabledProgressColor = DEFAULT_DISABLED_PROGRESS_COLOR

    @ColorInt
    private var disabledTrackColor = DEFAULT_DISABLED_TRACK_COLOR

    @ColorInt
    private var disabledSectionColor = DEFAULT_DISABLED_SECTION_COLOR

    @ColorInt
    private var progressColor: Int = 0

    @ColorInt
    private var trackColor: Int = 0

    @ColorInt
    private var sectionColor: Int = 0

    @ColorInt
    private var topBackgroundColor: Int = 0

    @ColorInt
    private var bottomBackgroundColor: Int = 0

    private var accuracyRank: Int = 0
    private var dragging: Boolean = false
    private var startX: Float = 0.toFloat()
    private var startY: Float = 0.toFloat()
    private var baseProgress: Int = 0

    private var _progress: Int = 0
    var progress: Int
        get() = _progress
        set(progress) = setProgressInternal(progress, false)

    var max: Int = 0
        set(value) {
            val max = value.coerceAtLeast(0)
            if (max == this.max) {
                return
            }
            field = max
            _progress = _progress.coerceAtMost(max)
            invalidate()
        }

    private var _chapterList: List<Int> = emptyList()
    var chapterList: List<Int>?
        get() = _chapterList
        set(chapterList) {
            _chapterList = chapterList ?: mutableListOf()
            invalidate()
        }

    interface ScrubBarListener {
        fun onProgressChanged(seekBar: ScrubBar, progress: Int, fromUser: Boolean)
        fun onStartTrackingTouch(seekBar: ScrubBar)
        fun onStopTrackingTouch(seekBar: ScrubBar)
        fun onAccuracyChanged(seekBar: ScrubBar, @Accuracy accuracy: Int)
    }

    init {
        enabledProgressColor = getColorAccent(context)
        val density = context.resources.displayMetrics.density
        trackWidth = TRACK_WIDTH_DP * density
        trackWidthHalf = trackWidth / 2
        smallThumbRadius = SMALL_THUMB_RADIUS_DP * density
        largeThumbRadius = LARGE_THUMB_RADIUS_DP * density
        scrubUnitLength = (SCRUB_UNIT_LENGTH_DP * density + 0.5f).toInt()
        paint = Paint()
        paint.strokeWidth = trackWidth
        paint.isAntiAlias = true
        progressColor = if (isEnabled) enabledProgressColor else disabledProgressColor
        trackColor = if (isEnabled) enabledTrackColor else disabledTrackColor
        sectionColor = if (isEnabled) enabledSectionColor else disabledSectionColor
    }

    fun setProgressColor(@ColorInt color: Int) {
        enabledProgressColor = color
        progressColor = if (isEnabled) enabledProgressColor else disabledProgressColor
        invalidate()
    }

    fun setTrackColor(@ColorInt color: Int) {
        enabledTrackColor = color
        trackColor = if (isEnabled) enabledTrackColor else disabledTrackColor
        invalidate()
    }

    fun setDisabledProgressColor(@ColorInt color: Int) {
        disabledProgressColor = color
        progressColor = if (isEnabled) enabledProgressColor else disabledProgressColor
        invalidate()
    }

    fun setDisabledTrackColor(@ColorInt color: Int) {
        disabledTrackColor = color
        trackColor = if (isEnabled) enabledTrackColor else disabledTrackColor
        invalidate()
    }

    fun setSectionColor(@ColorInt color: Int) {
        enabledSectionColor = color
        sectionColor = if (isEnabled) enabledSectionColor else disabledSectionColor
        invalidate()
    }

    fun setDisabledSectionColor(@ColorInt color: Int) {
        disabledSectionColor = color
        sectionColor = if (isEnabled) enabledSectionColor else disabledSectionColor
        invalidate()
    }

    fun setScrubUnitLength(length: Int) {
        scrubUnitLength = length
    }

    fun setTopBackgroundColor(@ColorInt color: Int) {
        topBackgroundColor = color
        invalidate()
    }

    fun setBottomBackgroundColor(@ColorInt color: Int) {
        bottomBackgroundColor = color
        invalidate()
    }

    private class SavedState : BaseSavedState {
        var progress: Int = 0
        var max: Int = 0
        var chapterList: List<Int> = emptyList()

        constructor(superState: Parcelable) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            progress = parcel.readInt()
            max = parcel.readInt()
            val n = parcel.readInt()
            if (n == 0) {
                chapterList = emptyList()
                return
            }
            val list = ArrayList<Int>(n)
            for (i in 0 until n) {
                list.add(parcel.readInt())
            }
            chapterList = list
        }

        override fun writeToParcel(
            out: Parcel,
            flags: Int,
        ) {
            super.writeToParcel(out, flags)
            out.writeInt(progress)
            out.writeInt(max)
            out.writeInt(chapterList.size)
            chapterList.forEach {
                out.writeInt(it)
            }
        }

        companion object CREATOR : Creator<SavedState> {
            override fun createFromParcel(`in`: Parcel): SavedState {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState() ?: return null
        val ss = SavedState(superState)
        ss.progress = _progress
        ss.max = max
        ss.chapterList = _chapterList
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        max = ss.max
        progress = ss.progress
        chapterList = ss.chapterList
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        progressColor = if (enabled) enabledProgressColor else disabledProgressColor
        trackColor = if (enabled) enabledTrackColor else disabledTrackColor
        sectionColor = if (enabled) enabledSectionColor else disabledSectionColor
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val w = (trackWidth + paddingLeft.toFloat() + paddingRight.toFloat()).toInt()
        val h = (trackWidth + paddingTop.toFloat() + paddingBottom.toFloat()).toInt()
        setMeasuredDimension(
            resolveSizeAndState(w, widthMeasureSpec, 0),
            resolveSizeAndState(h, heightMeasureSpec, 0),
        )
    }

    override fun onDraw(canvas: Canvas) {
        val paddingLeft = paddingLeft
        val areaWidth = (width - paddingLeft - paddingRight).toFloat()
        val areaHeight = (height - paddingTop - paddingBottom).toFloat()
        val cy = areaHeight / 2 + paddingTop
        val l = trackWidthHalf

        if (topBackgroundColor != 0) {
            canvas.save()
            canvas.clipRect(0f, 0f, width.toFloat(), cy + l)
            canvas.drawColor(topBackgroundColor)
            canvas.restore()
        }
        if (bottomBackgroundColor != 0) {
            canvas.save()
            canvas.clipRect(0f, cy - l, width.toFloat(), height.toFloat())
            canvas.drawColor(bottomBackgroundColor)
            canvas.restore()
        }
        paint.color = trackColor
        canvas.drawLine(paddingLeft.toFloat(), cy, areaWidth + paddingLeft, cy, paint)
        if (max == 0) {
            return
        }

        paint.color = progressColor
        val cx = _progress * areaWidth / max + paddingLeft
        canvas.drawLine(paddingLeft.toFloat(), cy, cx, cy, paint)
        val radius = if (dragging) largeThumbRadius else smallThumbRadius
        canvas.drawCircle(cx, cy, radius, paint)

        paint.color = sectionColor
        _chapterList.forEach {
            val sx = it * areaWidth / max + paddingLeft
            canvas.drawLine(sx - l, cy, sx + l, cy, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragging = true
                onTouchStart(event)
            }

            MotionEvent.ACTION_MOVE -> {
                if (dragging) {
                    onTouchMove(event)
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (dragging) {
                    dragging = false
                    onTouchEnd(event)
                }
            }

            else -> {
            }
        }
        return true
    }

    private fun onTouchStart(event: MotionEvent) {
        startX = event.x
        startY = event.y
        val progress = getProgressByPosition(event.x)
        baseProgress = progress
        accuracyRank = 0
        setProgressInternal(progress, true)
        scrubBarListener.onStartTrackingTouch(this)
        scrubBarListener.onAccuracyChanged(this, ACCURACY_RANKS[accuracyRank])
    }

    private fun onTouchMove(event: MotionEvent) {
        val dx = (event.x - startX) * ACCURACY[accuracyRank]
        val progressDiff = getProgressByDistance(dx)
        setProgressInternal(progressDiff + baseProgress, true)

        val distance = abs((event.y - startY).toInt())
        val rank = (distance / scrubUnitLength).coerceIn(0, RANK_MAX)
        if (accuracyRank != rank) {
            accuracyRank = rank
            baseProgress = _progress
            startX = event.x
            scrubBarListener.onAccuracyChanged(this, ACCURACY_RANKS[rank])
        }
    }

    private fun onTouchEnd(event: MotionEvent) {
        onTouchMove(event)
        scrubBarListener.onStopTrackingTouch(this)
        invalidate()
    }

    private fun getProgressByPosition(x: Float): Int {
        val left = paddingLeft
        val right = paddingRight
        val width = width - left - right
        return if (width == 0) 0 else ((x - left) * max / width).toInt()
    }

    private fun getProgressByDistance(dx: Float): Int {
        val left = paddingLeft
        val right = paddingRight
        val width = width - left - right
        return if (width == 0) 0 else (dx * max / width).toInt()
    }

    private fun setProgressInternal(
        progress: Int,
        fromUser: Boolean,
    ) {
        val newProgress = progress.coerceIn(0, max)
        if (newProgress == _progress) {
            return
        }
        _progress = newProgress
        scrubBarListener.onProgressChanged(this, newProgress, fromUser)
        invalidate()
    }

    fun setScrubBarListener(listener: ScrubBarListener?) {
        scrubBarListener = listener ?: LISTENER
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(ACCURACY_NORMAL, ACCURACY_HALF, ACCURACY_QUARTER)
    annotation class Accuracy

    companion object {
        @Dimension(unit = Dimension.DP)
        private val TRACK_WIDTH_DP = 3

        @Dimension(unit = Dimension.DP)
        private val SMALL_THUMB_RADIUS_DP = 4

        @Dimension(unit = Dimension.DP)
        private val LARGE_THUMB_RADIUS_DP = 8

        @Dimension(unit = Dimension.DP)
        private val SCRUB_UNIT_LENGTH_DP = 150

        @ColorInt
        private val DEFAULT_ENABLED_TRACK_COLOR = Color.argb(0x66, 0xff, 0xff, 0xff)

        @ColorInt
        private val DEFAULT_ENABLED_SECTION_COLOR = Color.argb(0xff, 0xff, 0xff, 0x0)

        @ColorInt
        private val DEFAULT_DISABLED_PROGRESS_COLOR = Color.argb(0xff, 0x80, 0x80, 0x80)

        @ColorInt
        private val DEFAULT_DISABLED_TRACK_COLOR = Color.argb(0x66, 0x60, 0x60, 0x60)

        @ColorInt
        private val DEFAULT_DISABLED_SECTION_COLOR = Color.argb(0x66, 0xff, 0xff, 0x0)

        const val ACCURACY_NORMAL = 0
        const val ACCURACY_HALF = 1
        const val ACCURACY_QUARTER = 2

        private val ACCURACY_RANKS = intArrayOf(
            ACCURACY_NORMAL,
            ACCURACY_HALF,
            ACCURACY_QUARTER,
        )
        private val ACCURACY = floatArrayOf(
            1.1f,
            0.5f,
            0.25f,
        )
        private val RANK_MAX = ACCURACY_RANKS.size - 1

        private val LISTENER: ScrubBarListener = object : ScrubBarListener {
            override fun onProgressChanged(
                seekBar: ScrubBar,
                progress: Int,
                fromUser: Boolean,
            ) = Unit

            override fun onStartTrackingTouch(seekBar: ScrubBar) = Unit
            override fun onStopTrackingTouch(seekBar: ScrubBar) = Unit
            override fun onAccuracyChanged(seekBar: ScrubBar, @Accuracy accuracy: Int) = Unit
        }

        private fun getColorAccent(context: Context): Int {
            val colorAttr = android.R.attr.colorAccent
            val outValue = TypedValue()
            context.theme.resolveAttribute(colorAttr, outValue, true)
            return outValue.data
        }
    }
}
