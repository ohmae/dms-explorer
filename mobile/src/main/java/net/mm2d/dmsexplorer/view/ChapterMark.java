/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import net.mm2d.dmsexplorer.R;

import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ChapterMark extends View {
    @NonNull
    private final Paint mPaint;
    private final float mHalfHeight;
    @Nullable
    private List<Integer> mChapterInfo;
    private int mDuration;

    public ChapterMark(@NonNull Context context) {
        this(context, null);
    }

    public ChapterMark(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChapterMark(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(context, R.color.chapterMark));
        mPaint.setAntiAlias(true);
        final Resources res = context.getResources();
        mHalfHeight = res.getDimension(R.dimen.chapter_height) / 2;
        mPaint.setStrokeWidth(res.getDimension(R.dimen.chapter_width));
    }

    public void setChapterInfo(@NonNull List<Integer> chapterInfo) {
        mChapterInfo = chapterInfo;
        invalidate();
    }

    public void setDuration(final int duration) {
        mDuration = duration;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDuration == 0 || mChapterInfo == null) {
            return;
        }
        final int width = getWidth() - getPaddingLeft() - getPaddingRight();
        final int margin = getPaddingLeft();
        final int cy = getHeight() / 2;
        for (final int position : mChapterInfo) {
            final int x = (int) (((float) position) / mDuration * width) + margin;
            canvas.drawLine(x, cy - mHalfHeight, x, cy + mHalfHeight, mPaint);
        }
    }
}
