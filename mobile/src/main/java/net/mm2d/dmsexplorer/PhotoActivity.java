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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.util.ImageViewUtils;
import net.mm2d.dmsexplorer.util.ViewLayoutUtils;

/**
 * 静止画表示のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PhotoActivity extends AppCompatActivity {
    private View mProgress;
    private ImageView mImageView;
    private View mToolbar;
    private FullscreenHelper mFullscreenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);
        final Intent intent = getIntent();
        final CdsObject object = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();

        findViewById(R.id.toolbarBack).setOnClickListener(view -> onBackPressed());
        final TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setText(object.getTitle());
        mToolbar = findViewById(R.id.toolbar);
        mFullscreenHelper = new FullscreenHelper.Builder(findViewById(R.id.root))
                .setTopView(mToolbar)
                .build();
        mFullscreenHelper.showNavigation();

        mImageView = (ImageView) findViewById(R.id.imageView);
        mProgress = findViewById(R.id.progressBar);
        mProgress.setVisibility(View.VISIBLE);

        downloadAndSetImage(uri.toString());
        adjustControlPanel();
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
        final Point p1 = DisplaySizeUtils.getSize(this);
        final Point p2 = DisplaySizeUtils.getRealSize(this);
        adjustControlPanel(p2.x - p1.x);
    }

    private void adjustControlPanel(int right) {
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
        super.onDestroy();
        mFullscreenHelper.onDestroy();
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
