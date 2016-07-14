/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.VideoView;

import net.mm2d.cds.CdsObject;

/**
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
        final Intent intent = getIntent();
        mObject = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        mHandler = new Handler();
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
            assert appBarLayout != null;
            appBarLayout.setElevation(0);
        }
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mObject.getTitle());
        mControlPanel = (ControlView) findViewById(R.id.controlPanel);
        assert mControlPanel != null;
        mControlPanel.setSyncVisibility(toolbar);
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
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mControlPanel.setVisible();
            }
        });
        mRoot.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                postHideNavigation();
            }
        });
        hideNavigation();
    }

    private void postHideNavigation() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideNavigation();
            }
        }, 3000);
    }

    private void hideNavigation() {
        final int visibility;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            visibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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
