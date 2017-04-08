/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.databinding.ControlViewBinding;
import net.mm2d.dmsexplorer.viewmodel.ControlViewModel;
import net.mm2d.util.Log;

/**
 * 動画、音楽再生においてプレーヤーのコントロールUIを提供するView。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ControlView extends FrameLayout implements OnPreparedListener {
    private static final String TAG = ControlView.class.getSimpleName();

    public interface OnUserActionListener {
        void onUserAction();
    }

    private static final OnUserActionListener ON_USER_ACTION_LISTENER = () -> {
    };
    private static final OnErrorListener ON_ERROR_LISTENER = (mp, what, extra) -> false;
    private static final OnInfoListener ON_INFO_LISTENER = (mp, what, extra) -> false;
    private static final OnCompletionListener ON_COMPLETION_LISTENER = mp -> {
    };

    private static final int MEDIA_ERROR_SYSTEM = -2147483648;
    private MediaPlayer mMediaPlayer;
    private boolean mTracking;
    private int mCurrentPosition;

    private ControlViewModel mModel;

    @NonNull
    private OnUserActionListener mOnUserActionListener = ON_USER_ACTION_LISTENER;
    @NonNull
    private OnErrorListener mOnErrorListener = ON_ERROR_LISTENER;
    @NonNull
    private OnInfoListener mOnInfoListener = ON_INFO_LISTENER;
    @NonNull
    private OnCompletionListener mOnCompletionListener = ON_COMPLETION_LISTENER;

    private final Runnable mGetPositionTask = new Runnable() {
        @Override
        public void run() {
            try {
                int sleep = 1000;
                if (!mTracking && mMediaPlayer.isPlaying()) {
                    final int duration = mMediaPlayer.getDuration();
                    final int position = mMediaPlayer.getCurrentPosition();
                    if (duration >= position) {
                        mModel.setProgress(position);
                        mCurrentPosition = position;
                    }
                    sleep = 1001 - position % 1000;
                }
                sleep = Math.min(Math.max(sleep, 100), 1000);
                removeCallbacks(this);
                postDelayed(this, sleep);
            } catch (final IllegalStateException ignored) {
            }
        }
    };

    private final OnErrorListener mMyOnErrorListener = new OnErrorListener() {
        private boolean mNoError = true;

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            logError(what, extra);
            mOnErrorListener.onError(mp, what, extra);
            if (mNoError) {
                mNoError = false;
                return false;
            }
            return true;
        }
    };

    private final OnInfoListener mMyOnInfoListener = (mp, what, extra) -> {
        logInfo(what, extra);
        mOnInfoListener.onInfo(mp, what, extra);
        return false;
    };
    private final OnCompletionListener mMyOnCompletionListener = (mp) -> {
        removeCallbacks(mGetPositionTask);
        mOnCompletionListener.onCompletion(mp);
    };

    public void setOnErrorListener(@Nullable OnErrorListener listener) {
        mOnErrorListener = listener != null ? listener : ON_ERROR_LISTENER;
    }

    public void setOnInfoListener(@Nullable OnInfoListener listener) {
        mOnInfoListener = listener != null ? listener : ON_INFO_LISTENER;
    }

    public void setOnCompletionListener(@Nullable OnCompletionListener listener) {
        mOnCompletionListener = listener != null ? listener : ON_COMPLETION_LISTENER;
    }

    public ControlView(@NonNull final Context context) {
        this(context, null);
    }

    public ControlView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlView(@NonNull final Context context, @Nullable final AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final ControlViewBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.control_view, this, true);
        mModel = new ControlViewModel();
        binding.setModel(mModel);
        setUpSeekBar(binding.seekBar);
        setUpPlayButton(binding.play);
    }

    private void setUpSeekBar(@NonNull final SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mModel.setProgressText(progress);
                    mOnUserActionListener.onUserAction();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTracking = false;
                if (mMediaPlayer == null) {
                    return;
                }
                mMediaPlayer.seekTo(seekBar.getProgress());
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    mModel.setPlaying(true);
                }
            }
        });
    }

    private void setUpPlayButton(@NonNull final View button) {
        button.setOnClickListener(v -> {
            if (mMediaPlayer == null) {
                return;
            }
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mModel.setPlaying(false);
            } else {
                mMediaPlayer.start();
                mModel.setPlaying(true);
            }
            mOnUserActionListener.onUserAction();
        });
    }

    public void setOnUserActionListener(@Nullable OnUserActionListener listener) {
        mOnUserActionListener = listener != null ? listener : ON_USER_ACTION_LISTENER;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mGetPositionTask);
    }

    @Override
    public void onPrepared(@NonNull final MediaPlayer mp) {
        mMediaPlayer = mp;
        mMediaPlayer.setOnErrorListener(mMyOnErrorListener);
        mMediaPlayer.setOnInfoListener(mMyOnInfoListener);
        mMediaPlayer.setOnCompletionListener(mMyOnCompletionListener);
        mMediaPlayer.setOnInfoListener(mMyOnInfoListener);
        mModel.setDuration(mMediaPlayer.getDuration());
        mModel.setPlaying(true);
        post(mGetPositionTask);
        mp.start();
        if (mCurrentPosition > 0) {
            mp.seekTo(mCurrentPosition);
        }
    }

    public void restoreSavePosition(int position) {
        mCurrentPosition = position;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
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
