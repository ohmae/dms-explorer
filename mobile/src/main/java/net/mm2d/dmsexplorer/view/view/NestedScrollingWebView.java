/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class NestedScrollingWebView extends WebView implements NestedScrollingChild {
    private final NestedScrollingChildHelper mHelper;
    private final int mTouchSlop;
    private float mStartY;
    private float mPrevY;
    private final int[] mConsumed = new int[2];
    private boolean mScrolling;

    public NestedScrollingWebView(final Context context) {
        this(context, null);
    }

    public NestedScrollingWebView(
            final Context context,
            final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollingWebView(
            final Context context,
            final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mScrolling) {
                    final int dy = (int) (mPrevY - event.getRawY());
                    dispatchNestedPreScroll(0, dy, mConsumed, null);
                    dispatchNestedScroll(0, mConsumed[1], 0, dy - mConsumed[1], null);
                } else {
                    if (Math.abs(mStartY - event.getRawY()) > mTouchSlop) {
                        mScrolling = true;
                        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mScrolling) {
                    stopNestedScroll();
                }
                mScrolling = false;
                break;
        }
        mPrevY = event.getRawY();
        return super.onTouchEvent(event);
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        mHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {
        return mHelper.isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int axes) {
        return mHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll() {
        mHelper.stopNestedScroll();
    }

    public boolean hasNestedScrollingParent() {
        return mHelper.hasNestedScrollingParent();
    }

    public boolean dispatchNestedScroll(
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed,
            int[] offsetInWindow) {
        return mHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    public boolean dispatchNestedPreScroll(
            int dx,
            int dy,
            int[] consumed,
            int[] offsetInWindow) {
        return mHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean dispatchNestedFling(
            float velocityX,
            float velocityY,
            boolean consumed) {
        return mHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(
            float velocityX,
            float velocityY) {
        return mHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
