/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import net.mm2d.android.cds.CdsObject;
import net.mm2d.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = "PhotoActivity";
    private Handler mHandler;
    private View mRoot;
    private Toolbar mToolbar;
    private CdsObject mObject;
    private View mProgress;
    private ImageView mImageView;
    private Bitmap mBitmap;

    private final Runnable mHideControlTask = new Runnable() {
        @Override
        public void run() {
            mToolbar.setVisibility(View.INVISIBLE);
        }
    };

    private void postHideControlTask() {
        mHandler.removeCallbacks(mHideControlTask);
        mHandler.postDelayed(mHideControlTask, 5000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_photo);
        final Intent intent = getIntent();
        mObject = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        mHandler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
            assert appBarLayout != null;
            appBarLayout.setElevation(0);
        }
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mObject.getTitle());
        mImageView = (ImageView) findViewById(R.id.imageView);
        mProgress = findViewById(R.id.progressBar);
        assert mProgress != null;
        mProgress.setVisibility(View.VISIBLE);
        new Thread(new GetImage(uri)).start();
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mToolbar.setVisibility(View.VISIBLE);
                postHideControlTask();
            }
        });
        mRoot.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                postHideNavigation();
            }
        });
        hideNavigation();
        postHideControlTask();
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

    private void setImage() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(mBitmap);
                mProgress.setVisibility(View.GONE);
            }
        });
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
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            mImageView.setImageBitmap(null);
        }
    }

    private class GetImage implements Runnable {
        private final Uri mUri;

        public GetImage(Uri uri) {
            mUri = uri;
        }

        @Override
        public void run() {
            try {
                final URL url = new URL(mUri.toString());
                final HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.connect();
                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return;
                }
                final InputStream is = con.getInputStream();
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1024];
                while (true) {
                    final int size = is.read(buffer);
                    if (size <= 0) {
                        break;
                    }
                    baos.write(buffer, 0, size);
                }
                is.close();
                con.disconnect();
                final byte[] array = baos.toByteArray();
                mBitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
                setImage();
            } catch (final IOException e) {
                Log.w(TAG, e);
            }
        }
    }
}
