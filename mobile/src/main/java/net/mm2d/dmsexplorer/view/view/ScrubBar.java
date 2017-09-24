/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ScrubBar extends View {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACCURACY_NORMAL, ACCURACY_HALF, ACCURACY_QUARTER})
    public @interface Accuracy {
    }

    public static final int ACCURACY_NORMAL = 0;
    public static final int ACCURACY_HALF = 1;
    public static final int ACCURACY_QUARTER = 2;

    private static final int[] ACCURACY_RANKS = {
            ACCURACY_NORMAL, ACCURACY_HALF, ACCURACY_QUARTER,
    };
    private static final float[] ACCURACY = new float[]{
            1.1f, 0.5f, 0.25f,
    };
    private static final int RANK_MAX = ACCURACY_RANKS.length - 1;

    public interface ScrubBarListener {
        void onProgressChanged(
                @NonNull ScrubBar seekBar,
                int progress,
                boolean fromUser);

        void onStartTrackingTouch(@NonNull ScrubBar seekBar);

        void onStopTrackingTouch(@NonNull ScrubBar seekBar);

        void onAccuracyChanged(
                @NonNull ScrubBar seekBar,
                @Accuracy int accuracy);
    }

    private static final ScrubBarListener LISTENER = new ScrubBarListener() {
        @Override
        public void onProgressChanged(
                @NonNull final ScrubBar seekBar,
                final int progress,
                final boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(@NonNull final ScrubBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull final ScrubBar seekBar) {
        }

        @Override
        public void onAccuracyChanged(
                @NonNull final ScrubBar seekBar,
                @Accuracy final int accuracy) {
        }
    };

    @Dimension(unit = Dimension.DP)
    private static final int TRACK_WIDTH_DP = 3;
    @Dimension(unit = Dimension.DP)
    private static final int SMALL_THUMB_RADIUS_DP = 4;
    @Dimension(unit = Dimension.DP)
    private static final int LARGE_THUMB_RADIUS_DP = 8;
    @Dimension(unit = Dimension.DP)
    private static final int SCRUB_UNIT_LENGTH_DP = 150;
    @ColorInt
    private static final int DEFAULT_ENABLED_TRACK_COLOR = Color.argb(0x66, 0xff, 0xff, 0xff);
    @ColorInt
    private static final int DEFAULT_ENABLED_SECTION_COLOR = Color.argb(0xff, 0xff, 0xff, 0x0);
    @ColorInt
    private static final int DEFAULT_DISABLED_PROGRESS_COLOR = Color.argb(0xff, 0x80, 0x80, 0x80);
    @ColorInt
    private static final int DEFAULT_DISABLED_TRACK_COLOR = Color.argb(0x66, 0x60, 0x60, 0x60);
    @ColorInt
    private static final int DEFAULT_DISABLED_SECTION_COLOR = Color.argb(0x66, 0xff, 0xff, 0x0);

    @NonNull
    private final Paint mPaint;
    @NonNull
    private ScrubBarListener mScrubBarListener = LISTENER;
    @Dimension(unit = Dimension.PX)
    private final float mTrackWidth;
    @Dimension(unit = Dimension.PX)
    private final float mTrackWidthHalf;
    @Dimension(unit = Dimension.PX)
    private final float mSmallThumbRadius;
    @Dimension(unit = Dimension.PX)
    private final float mLargeThumbRadius;
    @Dimension(unit = Dimension.PX)
    private int mScrubUnitLength;
    @ColorInt
    private int mEnabledProgressColor;
    @ColorInt
    private int mEnabledTrackColor = DEFAULT_ENABLED_TRACK_COLOR;
    @ColorInt
    private int mEnabledSectionColor = DEFAULT_ENABLED_SECTION_COLOR;
    @ColorInt
    private int mDisabledProgressColor = DEFAULT_DISABLED_PROGRESS_COLOR;
    @ColorInt
    private int mDisabledTrackColor = DEFAULT_DISABLED_TRACK_COLOR;
    @ColorInt
    private int mDisabledSectionColor = DEFAULT_DISABLED_SECTION_COLOR;
    @ColorInt
    private int mProgressColor;
    @ColorInt
    private int mTrackColor;
    @ColorInt
    private int mSectionColor;
    @ColorInt
    private int mTopBackgroundColor;
    @ColorInt
    private int mBottomBackgroundColor;

    private int mProgress;
    private int mMax;
    @NonNull
    private List<Integer> mChapterList = Collections.emptyList();

    private int mAccuracyRank;
    private boolean mDragging;
    private float mStartX;
    private float mStartY;
    private int mBaseProgress;

    public ScrubBar(@NonNull final Context context) {
        this(context, null);
    }

    public ScrubBar(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrubBar(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mEnabledProgressColor = getColorAccent(context);

        final float density = context.getResources().getDisplayMetrics().density;
        mTrackWidth = TRACK_WIDTH_DP * density;
        mTrackWidthHalf = mTrackWidth / 2;
        mSmallThumbRadius = SMALL_THUMB_RADIUS_DP * density;
        mLargeThumbRadius = LARGE_THUMB_RADIUS_DP * density;
        mScrubUnitLength = (int) (SCRUB_UNIT_LENGTH_DP * density + 0.5f);
        mPaint = new Paint();
        mPaint.setStrokeWidth(mTrackWidth);
        mPaint.setAntiAlias(true);
        mProgressColor = isEnabled() ? mEnabledProgressColor : mDisabledProgressColor;
        mTrackColor = isEnabled() ? mEnabledTrackColor : mDisabledTrackColor;
        mSectionColor = isEnabled() ? mEnabledSectionColor : mDisabledSectionColor;
    }

    private static int getColorAccent(@NonNull final Context context) {
        final int colorAttr = getColorAccentId(context);
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }

    private static int getColorAccentId(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return android.R.attr.colorAccent;
        }
        return context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
    }

    public void setProgressColor(@ColorInt final int color) {
        mEnabledProgressColor = color;
        mProgressColor = isEnabled() ? mEnabledProgressColor : mDisabledProgressColor;
        invalidate();
    }

    public void setTrackColor(@ColorInt final int color) {
        mEnabledTrackColor = color;
        mTrackColor = isEnabled() ? mEnabledTrackColor : mDisabledTrackColor;
        invalidate();
    }

    public void setDisabledProgressColor(@ColorInt final int color) {
        mDisabledProgressColor = color;
        mProgressColor = isEnabled() ? mEnabledProgressColor : mDisabledProgressColor;
        invalidate();
    }

    public void setDisabledTrackColor(@ColorInt final int color) {
        mDisabledTrackColor = color;
        mTrackColor = isEnabled() ? mEnabledTrackColor : mDisabledTrackColor;
        invalidate();
    }

    public void setSectionColor(@ColorInt final int color) {
        mEnabledSectionColor = color;
        mSectionColor = isEnabled() ? mEnabledSectionColor : mDisabledSectionColor;
        invalidate();
    }

    public void setDisabledSectionColor(@ColorInt final int color) {
        mDisabledSectionColor = color;
        mSectionColor = isEnabled() ? mEnabledSectionColor : mDisabledSectionColor;
        invalidate();
    }

    public void setScrubUnitLength(final int length) {
        mScrubUnitLength = length;
    }

    public void setTopBackgroundColor(@ColorInt final int color) {
        mTopBackgroundColor = color;
        invalidate();
    }

    public void setBottomBackgroundColor(@ColorInt final int color) {
        mBottomBackgroundColor = color;
        invalidate();
    }

    private static class SavedState extends BaseSavedState {
        private int progress;
        private int max;
        private List<Integer> chapterList;

        SavedState(@NonNull final Parcelable superState) {
            super(superState);
        }

        private SavedState(@NonNull final Parcel in) {
            super(in);
            progress = in.readInt();
            max = in.readInt();
            final int n = in.readInt();
            if (n == 0) {
                chapterList = Collections.emptyList();
                return;
            }
            chapterList = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                chapterList.add(in.readInt());
            }
        }

        @Override
        public void writeToParcel(
                @NonNull final Parcel out,
                final int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
            out.writeInt(max);
            out.writeInt(chapterList.size());
            for (final int chapter : chapterList) {
                out.writeInt(chapter);
            }
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(@NonNull final Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState ss = new SavedState(superState);
        ss.progress = mProgress;
        ss.max = mMax;
        ss.chapterList = mChapterList;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setMax(ss.max);
        setProgress(ss.progress);
        setChapterList(ss.chapterList);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        mProgressColor = enabled ? mEnabledProgressColor : mDisabledProgressColor;
        mTrackColor = enabled ? mEnabledTrackColor : mDisabledTrackColor;
        mSectionColor = enabled ? mEnabledSectionColor : mDisabledSectionColor;
    }

    @Override
    protected void onMeasure(
            final int widthMeasureSpec,
            final int heightMeasureSpec) {
        final int w = (int) (mTrackWidth + getPaddingLeft() + getPaddingRight());
        final int h = (int) (mTrackWidth + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(
                resolveSizeAndState(w, widthMeasureSpec, 0),
                resolveSizeAndState(h, heightMeasureSpec, 0));
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final int left = getPaddingLeft();
        final int right = getPaddingRight();
        final int top = getPaddingTop();
        final int bottom = getPaddingBottom();
        final int width = getWidth();
        final int height = getHeight();
        final float areaWidth = width - left - right;
        final float areaHeight = height - top - bottom;
        final float cy = areaHeight / 2 + top;
        final float l = mTrackWidthHalf;

        canvas.save();
        if (mTopBackgroundColor != 0) {
            canvas.clipRect(0, 0, width, cy + l);
            canvas.drawColor(mTopBackgroundColor);
        }
        canvas.restore();
        canvas.save();
        if (mBottomBackgroundColor != 0) {
            canvas.clipRect(0, cy - l, width, height);
            canvas.drawColor(mBottomBackgroundColor);
        }
        canvas.restore();

        mPaint.setColor(mTrackColor);
        canvas.drawLine(left, cy, areaWidth + left, cy, mPaint);
        if (mMax == 0) {
            return;
        }

        mPaint.setColor(mProgressColor);
        final float cx = mProgress * areaWidth / mMax + left;
        canvas.drawLine(left, cy, cx, cy, mPaint);
        final float radius = mDragging ? mLargeThumbRadius : mSmallThumbRadius;
        canvas.drawCircle(cx, cy, radius, mPaint);

        mPaint.setColor(mSectionColor);
        for (final int chapter : mChapterList) {
            final float sx = chapter * areaWidth / mMax + left;
            canvas.drawLine(sx - l, cy, sx + l, cy, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragging = true;
                onTouchStart(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mDragging) {
                    break;
                }
                onTouchMove(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mDragging) {
                    break;
                }
                mDragging = false;
                onTouchEnd(event);
                break;
            default:
                break;
        }
        return true;
    }

    private void onTouchStart(@NonNull final MotionEvent event) {
        mStartX = event.getX();
        mStartY = event.getY();
        final int progress = getProgressByPosition(event.getX());
        mBaseProgress = progress;
        mAccuracyRank = 0;
        setProgressInternal(progress, true);
        mScrubBarListener.onStartTrackingTouch(this);
        mScrubBarListener.onAccuracyChanged(this, ACCURACY_RANKS[mAccuracyRank]);
    }

    private void onTouchMove(@NonNull final MotionEvent event) {
        final float dx = (event.getX() - mStartX) * ACCURACY[mAccuracyRank];
        final int progressDiff = getProgressByDistance(dx);
        setProgressInternal(progressDiff + mBaseProgress, true);

        final int distance = Math.abs((int) (event.getY() - mStartY));
        final int rank = clamp(distance / mScrubUnitLength, 0, RANK_MAX);
        if (mAccuracyRank != rank) {
            mAccuracyRank = rank;
            mBaseProgress = mProgress;
            mStartX = event.getX();
            mScrubBarListener.onAccuracyChanged(this, ACCURACY_RANKS[rank]);
        }
    }

    private void onTouchEnd(@NonNull final MotionEvent event) {
        onTouchMove(event);
        mScrubBarListener.onStopTrackingTouch(this);
        invalidate();
    }

    private int getProgressByPosition(final float x) {
        final int left = getPaddingLeft();
        final int right = getPaddingRight();
        final int width = getWidth() - left - right;
        if (width == 0) {
            return 0;
        }
        return (int) ((x - left) * mMax / width);
    }

    private int getProgressByDistance(final float dx) {
        final int left = getPaddingLeft();
        final int right = getPaddingRight();
        final int width = getWidth() - left - right;
        if (width == 0) {
            return 0;
        }
        return (int) (dx * mMax / width);
    }

    public void setChapterList(@Nullable final List<Integer> chapterList) {
        mChapterList = chapterList != null ? chapterList : Collections.emptyList();
        invalidate();
    }

    @NonNull
    public List<Integer> getChapterList() {
        return mChapterList;
    }

    public void setMax(int max) {
        max = Math.max(0, max);
        if (max == mMax) {
            return;
        }
        mMax = max;
        mProgress = Math.min(mProgress, mMax);
        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public void setProgress(final int progress) {
        setProgressInternal(progress, false);
    }

    public int getProgress() {
        return mProgress;
    }

    private void setProgressInternal(
            int progress,
            boolean fromUser) {
        progress = clamp(progress, 0, mMax);
        if (progress == mProgress) {
            return;
        }
        mProgress = progress;
        mScrubBarListener.onProgressChanged(this, progress, fromUser);
        invalidate();
    }

    public void setScrubBarListener(@Nullable final ScrubBarListener listener) {
        mScrubBarListener = listener != null ? listener : LISTENER;
    }

    private static int clamp(
            int value,
            int min,
            int max) {
        return Math.min(Math.max(value, min), max);
    }
}
