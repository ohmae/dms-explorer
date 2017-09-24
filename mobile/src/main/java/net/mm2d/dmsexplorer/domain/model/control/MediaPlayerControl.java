/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model.control;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MediaPlayerControl implements MediaControl {
    @NonNull
    private final MediaPlayer mMediaPlayer;

    public MediaPlayerControl(@NonNull MediaPlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
    }

    @Override
    public void play() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void seekTo(final int position) {
        mMediaPlayer.seekTo(position);
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }

    @Override
    public int getCurrentPosition() {
        try {
            return mMediaPlayer.getCurrentPosition();
        } catch (final IllegalStateException ignored) {
            return 0;
        }
    }

    @Override
    public int getDuration() {
        try {
            return mMediaPlayer.getDuration();
        } catch (final IllegalStateException ignored) {
            return 0;
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return mMediaPlayer.isPlaying();
        } catch (final IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    public void setOnPreparedListener(@Nullable final OnPreparedListener listener) {
        mMediaPlayer.setOnPreparedListener(listener);
    }

    @Override
    public void setOnErrorListener(@Nullable final OnErrorListener listener) {
        mMediaPlayer.setOnErrorListener(listener);
    }

    @Override
    public void setOnInfoListener(@Nullable final OnInfoListener listener) {
        mMediaPlayer.setOnInfoListener(listener);
    }

    @Override
    public void setOnCompletionListener(@Nullable final OnCompletionListener listener) {
        mMediaPlayer.setOnCompletionListener(listener);
    }
}
