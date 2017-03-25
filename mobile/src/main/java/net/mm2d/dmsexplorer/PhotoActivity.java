/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.util.ImageViewUtils;

import java.util.concurrent.TimeUnit;

/**
 * 静止画表示のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = PhotoActivity.class.getSimpleName();
    private static final long NAVIGATION_INTERVAL = TimeUnit.SECONDS.toMillis(3);
    private Handler mHandler;
    private View mRoot;
    private View mProgress;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private View mToolbar;
    private Animation mEnterFromTop;
    private Animation mExitToTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);
        loadAnimation();
        final Intent intent = getIntent();
        final CdsObject object = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        mHandler = new Handler();

        findViewById(R.id.toolbarBack).setOnClickListener(view -> onBackPressed());
        final TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setText(object.getTitle());
        mToolbar = findViewById(R.id.toolbar);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mProgress = findViewById(R.id.progressBar);
        mProgress.setVisibility(View.VISIBLE);
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(v -> {
            showNavigation();
            postHideControlTask();
        });

        downloadAndSetImage(uri.toString());
        adjustControlPanel();
        showNavigation();
        postHideControlTask();
    }

    private void loadAnimation() {
        mEnterFromTop = AnimationUtils.loadAnimation(this, R.anim.enter_from_top);
        mExitToTop = AnimationUtils.loadAnimation(this, R.anim.exit_to_top);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustControlPanel();
    }

    private void adjustControlPanel() {
        if (VERSION.SDK_INT >= VERSION_CODES.N && isInMultiWindowMode()) {
            adjustControlPanel(0);
            return;
        }
        final Display display = getWindowManager().getDefaultDisplay();
        final Point p1 = new Point();
        display.getSize(p1);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            final Point p2 = new Point();
            display.getRealSize(p2);
            adjustControlPanel(p2.x - p1.x);
        } else {
            final View v = getWindow().getDecorView();
            v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    adjustControlPanel(v.getWidth() - p1.x);
                }
            });
        }
    }

    private void adjustControlPanel(int right) {
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

    private final Runnable mHideControlTask = this::hideNavigation;

    private void postHideControlTask() {
        mHandler.removeCallbacks(mHideControlTask);
        mHandler.postDelayed(mHideControlTask, NAVIGATION_INTERVAL);
    }

    private void showNavigation() {
        if (mToolbar.getVisibility() != View.VISIBLE) {
            mToolbar.startAnimation(mEnterFromTop);
            mToolbar.setVisibility(View.VISIBLE);
        }
        mRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    private void hideNavigation() {
        mToolbar.startAnimation(mExitToTop);
        mToolbar.setVisibility(View.GONE);
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
        super.onDestroy();
        mHandler.removeCallbacks(mHideControlTask);
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            mImageView.setImageBitmap(null);
        }
    }

    private void downloadAndSetImage(final @NonNull String url) {
        ImageViewUtils.downloadAndSetImage(mImageView, url, new ImageViewUtils.Callback() {
            @Override
            public void onSuccess() {
                mProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {
                showToast(R.string.toast_command_error_occurred);
            }
        });
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }
}
