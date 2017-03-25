/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.VideoView;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.view.ControlView;

import java.util.concurrent.TimeUnit;

/**
 * 動画再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MovieActivity extends AppCompatActivity {
    private static final long NAVIGATION_INTERVAL = TimeUnit.SECONDS.toMillis(3);
    private VideoView mVideoView;
    private View mRoot;
    private View mToolbar;
    private ControlView mControlPanel;
    private Handler mHandler;

    private Animation mEnterFromTop;
    private Animation mEnterFromBottom;
    private Animation mExitToTop;
    private Animation mExitToBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_activity);
        loadAnimation();
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(v -> {
            showNavigation();
            postHideNavigation();
        });
        mHandler = new Handler();
        final Intent intent = getIntent();
        final CdsObject object = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        findViewById(R.id.toolbarBack).setOnClickListener(view -> onBackPressed());
        final TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setText(object.getTitle());
        mToolbar = findViewById(R.id.toolbar);
        mControlPanel = (ControlView) findViewById(R.id.controlPanel);
        mControlPanel.setOnCompletionListener(mp -> onBackPressed());
        mControlPanel.setOnUserActionListener(this::postHideNavigation);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setOnPreparedListener(mControlPanel);
        mVideoView.setVideoURI(uri);
        mRoot.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                mControlPanel.setVisibility(View.VISIBLE);
            }
        });
        adjustControlPanel();
        showNavigation();
        postHideNavigation();
    }

    private void loadAnimation() {
        mEnterFromTop = AnimationUtils.loadAnimation(this, R.anim.enter_from_top);
        mEnterFromBottom = AnimationUtils.loadAnimation(this, R.anim.enter_from_bottom);
        mExitToTop = AnimationUtils.loadAnimation(this, R.anim.exit_to_top);
        mExitToBottom = AnimationUtils.loadAnimation(this, R.anim.exit_to_bottom);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustControlPanel();
    }

    private void adjustControlPanel() {
        if (VERSION.SDK_INT >= VERSION_CODES.N && isInMultiWindowMode()) {
            adjustControlPanel(0, 0);
            return;
        }
        final Display display = getWindowManager().getDefaultDisplay();
        final Point p1 = new Point();
        display.getSize(p1);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            final Point p2 = new Point();
            display.getRealSize(p2);
            adjustControlPanel(p2.x - p1.x, p2.y - p1.y);
        } else {
            final View v = getWindow().getDecorView();
            v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    adjustControlPanel(v.getWidth() - p1.x, v.getHeight() - p1.y);
                }
            });
        }
    }

    private void adjustControlPanel(int right, int bottom) {
        mControlPanel.setPadding(0, 0, 0, bottom);
        setLayoutMarginRight(mControlPanel, right);
        final int topPadding = getResources().getDimensionPixelSize(R.dimen.status_bar_size);
        mToolbar.setPadding(0, topPadding, 0, 0);
        setLayoutMarginRight(mToolbar, right);
    }

    private static void setLayoutMarginRight(View view, int rightMargin) {
        final LayoutParams params = view.getLayoutParams();
        if (!(params instanceof MarginLayoutParams)) {
            return;
        }
        final MarginLayoutParams marginParams = (MarginLayoutParams) params;
        marginParams.rightMargin = rightMargin;
        view.setLayoutParams(params);
    }

    private final Runnable mHideNavigationTask = this::hideNavigation;

    private void postHideNavigation() {
        mHandler.removeCallbacks(mHideNavigationTask);
        mHandler.postDelayed(mHideNavigationTask, NAVIGATION_INTERVAL);
    }

    private void showNavigation() {
        if (mToolbar.getVisibility() != View.VISIBLE) {
            mToolbar.startAnimation(mEnterFromTop);
            mToolbar.setVisibility(View.VISIBLE);
        }
        if (mControlPanel.getVisibility() != View.VISIBLE) {
            mControlPanel.startAnimation(mEnterFromBottom);
            mControlPanel.setVisibility(View.VISIBLE);
        }
        mRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    private void hideNavigation() {
        mToolbar.startAnimation(mExitToTop);
        mToolbar.setVisibility(View.GONE);
        mControlPanel.startAnimation(mExitToBottom);
        mControlPanel.setVisibility(View.GONE);
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
        mRoot.setSystemUiVisibility(visibility);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mVideoView.stopPlayback();
        super.onDestroy();
    }
}
