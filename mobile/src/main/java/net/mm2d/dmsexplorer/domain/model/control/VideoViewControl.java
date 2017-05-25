/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model.control;

import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.widget.VideoView;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class VideoViewControl implements MediaControl {
    private final VideoView mVideoView;

    public VideoViewControl(VideoView videoView) {
        mVideoView = videoView;
    }

    @Override
    public void play() {
        if (!mVideoView.isPlaying()) {
            mVideoView.start();
        }
    }

    @Override
    public void pause() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    public void seekTo(final int position) {
        mVideoView.seekTo(position);
        if (!mVideoView.isPlaying()) {
            mVideoView.start();
        }
    }

    @Override
    public void stop() {
        mVideoView.stopPlayback();
    }

    @Override
    public int getCurrentPosition() {
        try {
            return mVideoView.getCurrentPosition();
        } catch (final IllegalStateException ignored) {
            return 0;
        }
    }

    @Override
    public int getDuration() {
        try {
            return mVideoView.getDuration();
        } catch (final IllegalStateException ignored) {
            return 0;
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return mVideoView.isPlaying();
        } catch (final IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    public void setOnPreparedListener(final OnPreparedListener listener) {
        mVideoView.setOnPreparedListener(listener);
    }

    @Override
    public void setOnErrorListener(final OnErrorListener listener) {
        mVideoView.setOnErrorListener(listener);
    }

    @Override
    public void setOnInfoListener(final OnInfoListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mVideoView.setOnInfoListener(listener);
        }
    }

    @Override
    public void setOnCompletionListener(final OnCompletionListener listener) {
        mVideoView.setOnCompletionListener(listener);
    }
}
