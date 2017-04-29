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
    private static final String TAG = MediaPlayerModel.class.getSimpleName();
    private static final int SKIP_MARGIN = (int) TimeUnit.SECONDS.toMillis(3);
    private static final int MEDIA_ERROR_SYSTEM = -2147483648;
    private static final StatusListener STATUS_LISTENER = new StatusListenerAdapter();

    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private StatusListener mStatusListener = STATUS_LISTENER;
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
    public void setStatusListener(@NonNull StatusListener listener) {
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
    public void restoreSaveProgress(int progress) {
        mProgress = progress;
    }

    private void setProgress(int progress) {
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

    private void setDuration(int duration) {
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

    private void setPlaying(boolean playing) {
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
    public void seekTo(int position) {
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

    private void logError(int what, int extra) {
        final String wh;
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                wh = "MEDIA_ERROR_SERVER_DIED";
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                wh = "MEDIA_ERROR_UNKNOWN";
                break;
            default:
                wh = "";
                break;
        }
        final String ex;
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                ex = "MEDIA_ERROR_IO";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                ex = "MEDIA_ERROR_MALFORMED";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                ex = "MEDIA_ERROR_TIMED_OUT";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                ex = "MEDIA_ERROR_UNSUPPORTED";
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                ex = "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
                break;
            case MEDIA_ERROR_SYSTEM:
                ex = "MEDIA_ERROR_SYSTEM";
                break;
            default:
                ex = "";
                break;
        }
        Log.e(TAG, "onError:w" + what + " " + wh + " e" + extra + " " + ex);
    }

    private void logInfo(int what, int extra) {
        final String wh;
        switch (what) {
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                wh = "MEDIA_INFO_UNKNOWN";
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                wh = "MEDIA_INFO_VIDEO_TRACK_LAGGING";
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                wh = "MEDIA_INFO_VIDEO_RENDERING_START";
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                wh = "MEDIA_INFO_BUFFERING_START";
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                wh = "MEDIA_INFO_BUFFERING_END";
                break;
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                wh = "MEDIA_INFO_BAD_INTERLEAVING";
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                wh = "MEDIA_INFO_NOT_SEEKABLE";
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                wh = "MEDIA_INFO_METADATA_UPDATE";
                break;
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                wh = "MEDIA_INFO_UNSUPPORTED_SUBTITLE";
                break;
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                wh = "MEDIA_INFO_SUBTITLE_TIMED_OUT";
                break;
            default:
                wh = "";
                break;
        }
        Log.d(TAG, "onInfo:w:" + what + " " + wh + " e:" + extra);
    }
}
