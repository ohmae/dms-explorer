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
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;
import android.widget.VideoView;

import net.mm2d.android.cds.CdsObject;
import net.mm2d.dmsexplorer.ControlView.OnVisibilityChangeListener;

/**
 * 動画再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MovieActivity extends AppCompatActivity {
    private static final String TAG = "MovieActivity";
    private VideoView mVideoView;
    private View mRoot;
    private Handler mHandler;
    private ControlView mControlPanel;
    private CdsObject mObject;
    private final OnErrorListener mOnErrorListener = new OnErrorListener() {
        private boolean mNoError = true;

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (mNoError) {
                mNoError = false;
                return false;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_movie);
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mControlPanel.setVisible();
            }
        });
        final Intent intent = getIntent();
        mObject = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        mHandler = new Handler();
        findViewById(R.id.barBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        final TextView title = (TextView) findViewById(R.id.barTitle);
        title.setText(mObject.getTitle());
        final View toolbar = findViewById(R.id.toolbar);
        mControlPanel = (ControlView) findViewById(R.id.controlPanel);
        assert mControlPanel != null;
        mControlPanel.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            @Override
            public void onVisibilityChange(boolean visible) {
                toolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
                if (visible) {
                    showNavigation();
                } else {
                    hideNavigation();
                }
            }
        });
        mControlPanel.setAutoHide(true);
        mControlPanel.setVisible();
        mControlPanel.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onBackPressed();
            }
        });
        mControlPanel.setOnErrorListener(mOnErrorListener);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        assert mVideoView != null;
        mVideoView.setOnPreparedListener(mControlPanel);
        mVideoView.setVideoURI(uri);
        mRoot.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    mControlPanel.setVisibility(View.VISIBLE);
                }
            }
        });
        adjustControlPanel();
        showNavigation();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustControlPanel();
    }

    private void adjustControlPanel() {
        final Display display = getWindowManager().getDefaultDisplay();
        final Point p1 = new Point();
        display.getSize(p1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final Point p2 = new Point();
            display.getRealSize(p2);
            mControlPanel.setPadding(0, 0, p2.x - p1.x, p2.y - p1.y);
        } else {
            final View v = getWindow().getDecorView();
            v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mControlPanel.setPadding(0, 0, v.getWidth() - p1.x, v.getHeight() - p1.y);
                }
            });
        }
    }

    private void hideNavigation() {
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

    private void showNavigation() {
        mRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
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
