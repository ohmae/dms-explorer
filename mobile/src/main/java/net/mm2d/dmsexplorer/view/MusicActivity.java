/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.util.ImageViewUtils;
import net.mm2d.dmsexplorer.util.ThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.ContentPropertyAdapter;
import net.mm2d.dmsexplorer.view.view.ControlView;

import java.io.IOException;

/**
 * 音楽再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MusicActivity extends AppCompatActivity {
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
        setContentView(R.layout.music_activity);
        final Intent intent = getIntent();
        final CdsObject object = intent.getParcelableExtra(Const.EXTRA_OBJECT);
        final Uri uri = intent.getData();
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
        final int bgColor = ThemeUtils.getDeepColor(title);
        toolbar.setBackgroundColor(bgColor);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ThemeUtils.getDarkerColor(bgColor));
        }

        controlPanel.setBackgroundColor(bgColor);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detail);
        recyclerView.setAdapter(new ContentPropertyAdapter(this, object));
        final String albumArtUri = object.getValue(CdsObject.UPNP_ALBUM_ART_URI);
        if (albumArtUri != null) {
            downloadAndSetImage(albumArtUri);
        }
    }

    private void downloadAndSetImage(final @NonNull String url) {
        ImageViewUtils.downloadAndSetImage(mArtView, url, null);
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
