/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import net.mm2d.dmsexplorer.R;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class FullscreenHelper {
    public static class Builder {
        @NonNull
        private final View mRootView;
        @Nullable
        private View mTopView;
        @Nullable
        private View mBottomView;

        public Builder(@NonNull final View view) {
            mRootView = view;
        }

        @NonNull
        public Builder setTopView(@NonNull final View view) {
            mTopView = view;
            return this;
        }

        @NonNull
        public Builder setBottomView(@NonNull final View view) {
            mBottomView = view;
            return this;
        }

        @NonNull
        public FullscreenHelper build() {
            return new FullscreenHelper(this);
        }
    }

    private static final int SYSTEM_UI_VISIBLE;
    private static final int SYSTEM_UI_INVISIBLE;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SYSTEM_UI_VISIBLE = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            SYSTEM_UI_INVISIBLE = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        } else {
            SYSTEM_UI_VISIBLE = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            SYSTEM_UI_INVISIBLE = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
    }

    private static final long NAVIGATION_INTERVAL = TimeUnit.SECONDS.toMillis(3);
    @NonNull
    private final Handler mHandler;
    @NonNull
    private final View mRootView;
    @Nullable
    private final View mTopView;
    @Nullable
    private final View mBottomView;
    @NonNull
    private final Animation mEnterFromTop;
    @NonNull
    private final Animation mEnterFromBottom;
    @NonNull
    private final Animation mExitToTop;
    @NonNull
    private final Animation mExitToBottom;
    @NonNull
    private final Runnable mHideNavigationTask = this::hideNavigation;
    private boolean mPosted;
    private boolean mIsInPictureInPictureMode;

    private FullscreenHelper(@NonNull final Builder builder) {
        mHandler = new Handler();
        mRootView = builder.mRootView;
        mTopView = builder.mTopView;
        mBottomView = builder.mBottomView;

        final Context context = mRootView.getContext();
        mEnterFromTop = AnimationUtils.loadAnimation(context, R.anim.enter_from_top);
        mEnterFromBottom = AnimationUtils.loadAnimation(context, R.anim.enter_from_bottom);
        mExitToTop = AnimationUtils.loadAnimation(context, R.anim.exit_to_top);
        mExitToBottom = AnimationUtils.loadAnimation(context, R.anim.exit_to_bottom);

        mRootView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if (!mPosted && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                showNavigation();
            }
        });
    }

    private void postHideNavigation(final long interval) {
        mHandler.removeCallbacks(mHideNavigationTask);
        mHandler.postDelayed(mHideNavigationTask, interval);
        mPosted = true;
    }

    public boolean showNavigation() {
        return showNavigation(NAVIGATION_INTERVAL);
    }

    public boolean showNavigation(final long interval) {
        if (mIsInPictureInPictureMode) {
            return false;
        }
        boolean execute = false;
        if (mTopView != null && mTopView.getVisibility() != View.VISIBLE) {
            mTopView.clearAnimation();
            mTopView.startAnimation(mEnterFromTop);
            mTopView.setVisibility(View.VISIBLE);
            execute = true;
        }
        if (mBottomView != null && mBottomView.getVisibility() != View.VISIBLE) {
            mBottomView.clearAnimation();
            mBottomView.startAnimation(mEnterFromBottom);
            mBottomView.setVisibility(View.VISIBLE);
            execute = true;
        }
        mRootView.setSystemUiVisibility(SYSTEM_UI_VISIBLE);
        postHideNavigation(interval);
        return execute;
    }

    private void hideNavigation() {
        mPosted = false;
        if (mTopView != null) {
            mTopView.clearAnimation();
            mTopView.startAnimation(mExitToTop);
            mTopView.setVisibility(View.GONE);
        }
        if (mBottomView != null) {
            mBottomView.clearAnimation();
            mBottomView.startAnimation(mExitToBottom);
            mBottomView.setVisibility(View.GONE);
        }
        mRootView.setSystemUiVisibility(SYSTEM_UI_INVISIBLE);
    }

    public void onPictureInPictureModeChanged(final boolean isInPictureInPictureMode) {
        mIsInPictureInPictureMode = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            mHandler.removeCallbacks(mHideNavigationTask);
            if (mTopView != null) {
                mTopView.clearAnimation();
                mTopView.setVisibility(View.GONE);
            }
            if (mBottomView != null) {
                mBottomView.clearAnimation();
                mBottomView.setVisibility(View.GONE);
            }
        } else {
            showNavigation();
        }
    }

    public void terminate() {
        mHandler.removeCallbacks(mHideNavigationTask);
    }
}
