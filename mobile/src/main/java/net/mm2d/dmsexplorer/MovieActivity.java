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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.util.DisplaySizeUtils;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.util.ViewLayoutUtils;
import net.mm2d.dmsexplorer.view.ControlView;

/**
 * 動画再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MovieActivity extends AppCompatActivity {
    private VideoView mVideoView;
    private View mToolbar;
    private ControlView mControlPanel;
    private FullscreenHelper mFullscreenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_activity);
        final Intent intent = getIntent();
        final CdsObject object = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        findViewById(R.id.toolbarBack).setOnClickListener(view -> onBackPressed());

        final TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setText(object.getTitle());
        mToolbar = findViewById(R.id.toolbar);
        mControlPanel = (ControlView) findViewById(R.id.controlPanel);
        mFullscreenHelper = new FullscreenHelper.Builder(findViewById(R.id.root))
                .setTopView(mToolbar)
                .setBottomView(mControlPanel)
                .build();

        mControlPanel.setOnCompletionListener(mp -> onBackPressed());
        mControlPanel.setOnUserActionListener(mFullscreenHelper::postHideNavigation);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setOnPreparedListener(mControlPanel);
        mVideoView.setVideoURI(uri);
        adjustControlPanel();
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
        final Point p2 = DisplaySizeUtils.getRealSize(display);
        adjustControlPanel(p2.x - p1.x, p2.y - p1.y);
    }

    private void adjustControlPanel(int right, int bottom) {
        mControlPanel.setPadding(0, 0, 0, bottom);
        final int height = getResources().getDimensionPixelOffset(R.dimen.control_height);
        ViewLayoutUtils.setLayoutHeight(mControlPanel, height + bottom);
        ViewLayoutUtils.setLayoutMarginRight(mControlPanel, right);
        final int topPadding = getResources().getDimensionPixelSize(R.dimen.status_bar_size);
        mToolbar.setPadding(0, topPadding, 0, 0);
        ViewLayoutUtils.setLayoutMarginRight(mToolbar, right);
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
        mFullscreenHelper.onDestroy();
    }
}
