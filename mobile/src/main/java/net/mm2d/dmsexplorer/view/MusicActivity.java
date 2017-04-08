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
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.util.ImageViewUtils;
import net.mm2d.dmsexplorer.viewmodel.MusicActivityModel;

import java.io.IOException;

/**
 * 音楽再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MusicActivity extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    private MusicActivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.music_activity);
        final MusicActivityModel model = MusicActivityModel.create(this, Repository.getInstance());
        if (model == null) {
            finish();
            return;
        }
        mBinding.setModel(model);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.controlPanel.setOnCompletionListener(mp -> onBackPressed());
        final Repository repository = Repository.getInstance();
        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mBinding.controlPanel);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(this, targetModel.getUri());
            mMediaPlayer.prepareAsync();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final String albumArtUri = targetModel.getCdsObject().getValue(CdsObject.UPNP_ALBUM_ART_URI);
        if (albumArtUri != null) {
            ImageViewUtils.downloadAndSetImage(mBinding.art, albumArtUri, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
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
