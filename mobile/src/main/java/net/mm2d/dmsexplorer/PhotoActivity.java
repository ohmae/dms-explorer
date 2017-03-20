/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.BitmapUtils;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);
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
        new Thread(new GetImage(uri)).start();
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(v -> {
            showNavigation();
            postHideControlTask();
        });
        showNavigation();
        postHideControlTask();
    }

    private final Runnable mHideControlTask = this::hideNavigation;

    private void postHideControlTask() {
        mHandler.removeCallbacks(mHideControlTask);
        mHandler.postDelayed(mHideControlTask, NAVIGATION_INTERVAL);
    }

    private void showNavigation() {
        mToolbar.setVisibility(View.VISIBLE);
        mRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    private void hideNavigation() {
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
            final byte[] data = downloadData(mUri.toString());
            if (data == null) {
                return;
            }
            mHandler.post(() -> setImage(data));
        }
    }

    private void setImage(final @NonNull byte[] data) {
        ViewUtils.execAfterAllocateSize(mImageView, () -> setImageInner(data));
    }

    private void setImageInner(final @NonNull byte[] data) {
        mBitmap = BitmapUtils.decodeBitmap(data, mImageView.getWidth(), mImageView.getHeight());
        mImageView.setImageBitmap(mBitmap);
        mProgress.setVisibility(View.GONE);
    }

    @Nullable
    private static byte[] downloadData(@NonNull String uri) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        try {
            final URL url = new URL(uri);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
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
            return baos.toByteArray();
        } catch (final IOException e) {
            Log.w(TAG, e);
        }
        return null;
    }
}
