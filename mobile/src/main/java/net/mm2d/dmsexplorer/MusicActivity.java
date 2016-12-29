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
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;

import net.mm2d.android.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.LaunchUtils;
import net.mm2d.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 音楽再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MusicActivity extends AppCompatActivity implements PropertyAdapter.OnItemLinkClickListener {
    private static final String TAG = "MusicActivity";
    private Handler mHandler;
    private MediaPlayer mMediaPlayer;
    private ImageView mArtView;
    private Bitmap mBitmap;
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
        setContentView(R.layout.act_music);
        final Intent intent = getIntent();
        final CdsObject object = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
        mHandler = new Handler();
        mArtView = (ImageView) findViewById(R.id.art);

        final ControlView controlPanel = (ControlView) findViewById(R.id.controlPanel);
        controlPanel.setOnErrorListener(mOnErrorListener);
        controlPanel.setOnCompletionListener(mp -> onBackPressed());

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(controlPanel);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepareAsync();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        final String title = AribUtils.toDisplayableString(object.getTitle());
        actionBar.setTitle(title);
        final int bgColor = ThemeUtils.getAccentColor(object.getTitle());
        actionBar.setBackgroundDrawable(new ColorDrawable(bgColor));

        controlPanel.setBackgroundColor(bgColor);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detail);
        final PropertyAdapter adapter = new PropertyAdapter(this);
        adapter.setOnItemLinkClickListener(this);
        CdsDetailFragment.setupPropertyAdapter(this, adapter, object);
        recyclerView.setAdapter(adapter);
        final String albumArtUri = object.getValue(CdsObject.UPNP_ALBUM_ART_URI);
        if (albumArtUri != null) {
            new Thread(new GetImage(albumArtUri)).start();
        }
    }

    private class GetImage implements Runnable {
        private final String mUri;

        public GetImage(String uri) {
            mUri = uri;
        }

        @Override
        public void run() {
            final byte[] data = downloadData(mUri);
            if (data == null) {
                return;
            }
            setImage(data);
        }
    }

    private void setImage(@NonNull byte[] data) {
        mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        mHandler.post(() -> mArtView.setImageBitmap(mBitmap));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            mArtView.setImageBitmap(null);
        }
    }

    @Override
    public void onItemLinkClick(String link) {
        LaunchUtils.openUri(this, link);
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
}
