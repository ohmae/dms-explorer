/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.util.ViewLayoutUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
@SuppressLint("ViewConstructor")
public class IntroductoryOverlay extends RelativeLayout {
    private static final long ANIMATION_DURATION = 400;
    private static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
    @NonNull
    private final Activity mActivity;
    @NonNull
    private final View mView;
    @ColorInt
    private final int mDimmerColor;
    @NonNull
    private final Paint mCirclePaint;
    @NonNull
    private final Paint mErasePaint;
    @NonNull
    private final TextView mTitle;
    @NonNull
    private final Runnable mRemoveTask = this::dismiss;
    private final long mTimeout;
    private final int mMargin;

    @Nullable
    private Bitmap mBitmap;
    @Nullable
    private Canvas mCanvas;
    private float mCenterY;
    private float mCenterX;
    private float mHoleRadius;
    private float mCircleRadiusStart;
    private float mCircleRadius;
    private ValueAnimator mAnimator;
    private boolean mIsVisible;

    private IntroductoryOverlay(@NonNull final Builder builder) {
        super(builder.mActivity, null, 0);
        mDimmerColor = builder.mDimmerColor;
        mActivity = builder.mActivity;
        mView = builder.mView;
        mTimeout = builder.mTimeout < ANIMATION_DURATION ? DEFAULT_TIMEOUT : builder.mTimeout;

        LayoutInflater.from(getContext()).inflate(R.layout.introductory_overlay, this);
        mTitle = (TextView) findViewById(R.id.title);
        if (!TextUtils.isEmpty(builder.mTitleText)) {
            mTitle.setText(builder.mTitleText);
        }
        final TextView subtitle = (TextView) findViewById(R.id.subtitle);
        if (!TextUtils.isEmpty(builder.mSubtitleText)) {
            subtitle.setText(builder.mSubtitleText);
        }
        mMargin = mActivity.getResources()
                .getDimensionPixelSize(R.dimen.introduction_title_margin_horizontal);
        mCirclePaint = makeCirclePaint(builder.mOverlayColor);
        mErasePaint = makeErasePaint();
    }

    private static Paint makeCirclePaint(@ColorInt final int color) {
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setAntiAlias(true);
        return paint;
    }

    private static Paint makeErasePaint() {
        final Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setAntiAlias(true);
        return paint;
    }

    public void show() {
        if (mIsVisible) {
            return;
        }
        mIsVisible = true;
        adjustNavigation();
        ViewUtils.execAfterAllocateSize(mView, () -> {
            setUpDrawingParam();
            setUpAnimation();
            ((ViewGroup) mActivity.getWindow().getDecorView()).addView(this);
            postDelayed(mRemoveTask, mTimeout);
        });
    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        adjustNavigation();
        ViewUtils.execOnLayout(mView, () -> {
            setUpDrawingParam();
            invalidate();
        });
    }

    private void setUpAnimation() {
        mAnimator = ValueAnimator.ofFloat(mCircleRadiusStart, mCircleRadius);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(animation -> invalidate());
        mAnimator.start();
    }

    private void adjustNavigation() {
        final Point size = DisplaySizeUtils.getNavigationBarArea(mActivity);
        ViewLayoutUtils.setLayoutMarginRight(mTitle, mMargin + size.x);
    }

    public void setUpDrawingParam() {
        final Rect rect = new Rect();
        mView.getGlobalVisibleRect(rect);
        mCenterX = rect.centerX();
        mCenterY = rect.centerY();
        mHoleRadius = calcHoleRadius(rect);
        mCircleRadius = calcCircleRadius(mActivity);
        mCircleRadiusStart = calcCircleRadiusStart(mActivity);
    }

    private static float calcHoleRadius(@NonNull final Rect viewRect) {
        final int w = viewRect.width();
        final int h = viewRect.height();
        return (float) Math.sqrt(w * w + h * h) / 2f;
    }

    private static float calcCircleRadius(@NonNull final Activity activity) {
        final Point size = DisplaySizeUtils.getRealSize(activity);
        final int x = size.x;
        final int y = size.y;
        return Math.min(x, y);
    }

    private static float calcCircleRadiusStart(@NonNull final Activity activity) {
        final Point size = DisplaySizeUtils.getRealSize(activity);
        final int x = size.x;
        final int y = size.y;
        return (float) Math.sqrt(x * x + y * y);
    }

    private void dismiss() {
        removeCallbacks(mRemoveTask);
        if (!mIsVisible) {
            return;
        }
        mIsVisible = false;
        final ObjectAnimator fade = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
        fade.setDuration(ANIMATION_DURATION);
        fade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                dismissInner();
            }
        });
        fade.start();
    }

    private void dismissInner() {
        if (getContext() != null) {
            ((ViewGroup) mActivity.getWindow().getDecorView()).removeView(this);
        }
        recycleBitmap();
    }

    private void recycleBitmap() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mCanvas = null;
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        if (mBitmap == null || mBitmap.getWidth() != getWidth() || mBitmap.getHeight() != getHeight()) {
            recycleBitmap();
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        final float radius = (float) mAnimator.getAnimatedValue();
        mCanvas.drawColor(mDimmerColor, PorterDuff.Mode.SRC);
        mCanvas.drawCircle(mCenterX, mCenterY, radius, mCirclePaint);
        mCanvas.drawCircle(mCenterX, mCenterY, mHoleRadius, mErasePaint);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!mAnimator.isRunning() && event.getAction() == MotionEvent.ACTION_UP) {
            dismiss();
        }
        return true;
    }

    public static class Builder {
        @NonNull
        private final Activity mActivity;
        private String mTitleText;
        private String mSubtitleText;
        private View mView;
        private long mTimeout;
        @ColorInt
        private int mOverlayColor;
        @ColorInt
        private int mDimmerColor;

        public Builder(@NonNull final Activity activity) {
            mActivity = activity;
        }

        public IntroductoryOverlay build() {
            if (mView == null) {
                throw new IllegalStateException("View can't be null");
            }
            if (mTitleText == null) {
                throw new IllegalStateException("TitleText can't be null");
            }
            if (mSubtitleText == null) {
                throw new IllegalStateException("SubtitleText can't be null");
            }
            return new IntroductoryOverlay(this);
        }

        public Builder setView(@NonNull final View view) {
            mView = view;
            return this;
        }

        public Builder setTimeout(final long timeout) {
            mTimeout = timeout;
            return this;
        }

        public Builder setOverlayColor(@ColorRes final int colorId) {
            mOverlayColor = ContextCompat.getColor(mActivity, colorId);
            return this;
        }

        public Builder setDimmerColor(@ColorRes final int colorId) {
            mDimmerColor = ContextCompat.getColor(mActivity, colorId);
            return this;
        }

        public Builder setTitleText(@StringRes final int stringId) {
            mTitleText = mActivity.getString(stringId);
            return this;
        }

        public Builder setTitleText(@NonNull final String text) {
            mTitleText = text;
            return this;
        }

        public Builder setSubtitleText(@StringRes final int stringId) {
            mSubtitleText = mActivity.getString(stringId);
            return this;
        }

        public Builder setSubtitleText(@NonNull final String text) {
            mSubtitleText = text;
            return this;
        }
    }
}
