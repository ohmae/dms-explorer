/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.domain.model.control.MediaControl;
import net.mm2d.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public abstract class MediaPlayerModel implements PlayerModel, OnPreparedListener {
    private static final int SKIP_MARGIN = (int) TimeUnit.SECONDS.toMillis(3);
    private static final int MEDIA_ERROR_SYSTEM = -2147483648;
    private static final StatusListener STATUS_LISTENER = new StatusListenerAdapter();

    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private StatusListener mStatusListener = STATUS_LISTENER;
    @NonNull
    private final MediaControl mMediaControl;
    private boolean mPlaying;
    private int mProgress;
    private int mDuration;
    private boolean mTerminated;

    private final Runnable mGetPositionTask = new Runnable() {
        @Override
        public void run() {
            try {
                int sleep = 1000;
                final boolean playing = mMediaControl.isPlaying();
                setPlaying(playing);
                if (playing) {
                    final int duration = mMediaControl.getDuration();
                    final int position = mMediaControl.getCurrentPosition();
                    if (duration >= position) {
                        setProgress(position);
                        setDuration(duration);
                    }
                    sleep = 1001 - position % 1000;
                }
                sleep = Math.min(Math.max(sleep, 100), 1000);
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, sleep);
            } catch (final IllegalStateException ignored) {
            }
        }
    };

    MediaPlayerModel(@NonNull final MediaControl mediaControl) {
        mMediaControl = mediaControl;
        mMediaControl.setOnPreparedListener(this);
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @CallSuper
    @Override
    public void terminate() {
        if (mTerminated) {
            return;
        }
        mMediaControl.setOnPreparedListener(null);
        mMediaControl.setOnErrorListener(null);
        mMediaControl.setOnInfoListener(null);
        mMediaControl.setOnCompletionListener(null);
        mMediaControl.stop();
        mProgress = 0;
        mTerminated = true;
    }

    @Override
    public void setStatusListener(@NonNull final StatusListener listener) {
        mStatusListener = listener;
        mMediaControl.setOnErrorListener((mp, what, extra) -> {
            logError(what, extra);
            return mStatusListener.onError(what, extra);
        });
        mMediaControl.setOnInfoListener((mp, what, extra) -> {
            logInfo(what, extra);
            return mStatusListener.onInfo(what, extra);
        });
        mMediaControl.setOnCompletionListener((mp) -> {
            mHandler.removeCallbacks(mGetPositionTask);
            mStatusListener.onCompletion();
        });
    }

    @Override
    public void restoreSaveProgress(final int progress) {
        mProgress = progress;
    }

    private void setProgress(final int progress) {
        if (mProgress == progress) {
            return;
        }
        mProgress = progress;
        mStatusListener.notifyProgress(progress);
    }

    @Override
    public int getProgress() {
        return mProgress;
    }

    private void setDuration(final int duration) {
        if (mDuration == duration) {
            return;
        }
        mDuration = duration;
        mStatusListener.notifyDuration(duration);
    }

    @Override
    public int getDuration() {
        return mDuration;
    }

    @Override
    public boolean isPlaying() {
        return mPlaying;
    }

    private void setPlaying(final boolean playing) {
        if (mPlaying == playing) {
            return;
        }
        mPlaying = playing;
        mStatusListener.notifyPlayingState(playing);
    }

    @Override
    public void play() {
        mMediaControl.play();
    }

    @Override
    public void pause() {
        mMediaControl.pause();
    }

    @Override
    public void seekTo(final int position) {
        mMediaControl.seekTo(position);
        mHandler.removeCallbacks(mGetPositionTask);
        mHandler.post(mGetPositionTask);
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public boolean previous() {
        if (mMediaControl.getCurrentPosition() < SKIP_MARGIN) {
            return false;
        }
        seekTo(0);
        return true;
    }

    @Override
    public void onPrepared(@NonNull final MediaPlayer mediaPlayer) {
        setDuration(getDuration());
        mHandler.post(mGetPositionTask);
        play();
        if (mProgress > 0) {
            seekTo(mProgress);
        }
        setPlaying(isPlaying());
    }

    private void logError(
            final int what,
            final int extra) {
        Log.e("onError:w" + what + " " + getErrorWhatString(what)
                + " e" + extra + " " + getErrorExtraString(extra));
    }

    private void logInfo(
            final int what,
            final int extra) {
        Log.d("onInfo:w:" + what + " " + getInfoWhatString(what)
                + " e:" + extra);
    }

    @NonNull
    private String getErrorWhatString(final int what) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                return "MEDIA_ERROR_SERVER_DIED";
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                return "MEDIA_ERROR_UNKNOWN";
            default:
                return "";
        }
    }

    @NonNull
    private String getErrorExtraString(final int extra) {
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                return "MEDIA_ERROR_IO";
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                return "MEDIA_ERROR_MALFORMED";
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                return "MEDIA_ERROR_TIMED_OUT";
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                return "MEDIA_ERROR_UNSUPPORTED";
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                return "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
            case MEDIA_ERROR_SYSTEM:
                return "MEDIA_ERROR_SYSTEM";
            default:
                return "";
        }
    }

    @NonNull
    private String getInfoWhatString(final int what) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                return "MEDIA_INFO_UNKNOWN";
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                return "MEDIA_INFO_VIDEO_TRACK_LAGGING";
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                return "MEDIA_INFO_VIDEO_RENDERING_START";
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                return "MEDIA_INFO_BUFFERING_START";
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                return "MEDIA_INFO_BUFFERING_END";
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                return "MEDIA_INFO_BAD_INTERLEAVING";
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                return "MEDIA_INFO_NOT_SEEKABLE";
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                return "MEDIA_INFO_METADATA_UPDATE";
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                return "MEDIA_INFO_UNSUPPORTED_SUBTITLE";
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                return "MEDIA_INFO_SUBTITLE_TIMED_OUT";
            default:
                return "";
        }
    }
}
