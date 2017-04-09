/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.MusicActivityBinding;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.util.ImageViewUtils;
import net.mm2d.dmsexplorer.viewmodel.MusicActivityModel;
import net.mm2d.util.Log;

import java.io.IOException;

/**
 * 音楽再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MusicActivity extends AppCompatActivity {
    private static final String TAG = MusicActivity.class.getSimpleName();
    private static final String KEY_POSITION = "KEY_POSITION";
    private MediaPlayer mMediaPlayer;
    private MusicActivityBinding mBinding;
    private Repository mRepository;
    private MediaServerModel mServerModel;
    private PlaybackTargetModel mTargetModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepository = Repository.get();
        mBinding = DataBindingUtil.setContentView(this, R.layout.music_activity);
        final MusicActivityModel model = MusicActivityModel.create(this, mRepository);
        if (model == null) {
            finish();
            return;
        }
        mBinding.setModel(model);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.controlPanel.setOnCompletionListener(mp -> onCompletion());

        if (savedInstanceState != null) {
            mBinding.controlPanel.restoreSavePosition(savedInstanceState.getInt(KEY_POSITION, 0));
        }
        mServerModel = mRepository.getMediaServerModel();
        prepareMediaPlayer();
    }

    private void prepareMediaPlayer() {
        mTargetModel = mRepository.getPlaybackTargetModel();
        final String albumArtUri = mTargetModel.getCdsObject().getValue(CdsObject.UPNP_ALBUM_ART_URI);
        if (albumArtUri != null) {
            ImageViewUtils.downloadAndSetImage(mBinding.art, albumArtUri, null);
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mBinding.controlPanel);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(this, mTargetModel.getUri());
            mMediaPlayer.prepareAsync();
        } catch (final IOException e) {
            Log.w(TAG, e);
        }
    }

    private void onCompletion() {
        if (!mServerModel.selectNextObject()) {
            onBackPressed();
            return;
        }
        mBinding.controlPanel.reset();
        mBinding.setModel(MusicActivityModel.create(this, mRepository));
        prepareMediaPlayer();
    }

    private void terminateMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mBinding.controlPanel.getCurrentPosition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        terminateMediaPlayer();
        mBinding.art.setImageBitmap(null);
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
