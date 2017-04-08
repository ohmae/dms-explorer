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
        private final View mRootView;
        private View mTopView;
        private View mBottomView;

        public Builder(@NonNull final View view) {
            mRootView = view;
        }

        public Builder setTopView(@NonNull final View view) {
            mTopView = view;
            return this;
        }

        public Builder setBottomView(@NonNull final View view) {
            mBottomView = view;
            return this;
        }

        public FullscreenHelper build() {
            return new FullscreenHelper(this);
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

        mRootView.setOnClickListener(v -> showNavigation());
        mRootView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                showNavigation();
            }
        });
    }

    private final Runnable mHideNavigationTask = this::hideNavigation;

    public void postHideNavigation() {
        mHandler.removeCallbacks(mHideNavigationTask);
        mHandler.postDelayed(mHideNavigationTask, NAVIGATION_INTERVAL);
    }

    public void showNavigation() {
        if (mTopView != null && mTopView.getVisibility() != View.VISIBLE) {
            mTopView.startAnimation(mEnterFromTop);
            mTopView.setVisibility(View.VISIBLE);
        }
        if (mBottomView != null && mBottomView.getVisibility() != View.VISIBLE) {
            mBottomView.startAnimation(mEnterFromBottom);
            mBottomView.setVisibility(View.VISIBLE);
        }
        mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        postHideNavigation();
    }

    private void hideNavigation() {
        if (mTopView != null) {
            mTopView.startAnimation(mExitToTop);
            mTopView.setVisibility(View.GONE);
        }
        if (mBottomView != null) {
            mBottomView.startAnimation(mExitToBottom);
            mBottomView.setVisibility(View.GONE);
        }
        final int visibility;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            visibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        } else {
            visibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        mRootView.setSystemUiVisibility(visibility);
    }

    public void onDestroy() {
        mHandler.removeCallbacks(mHideNavigationTask);
    }
}
