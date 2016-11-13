/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import net.mm2d.util.Log;

import java.util.Locale;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ControlView extends LinearLayout
        implements OnPreparedListener {
    private static final String TAG = "ControlView";
    private MediaPlayer mMediaPlayer;
    private TextView mProgress;
    private TextView mDuration;
    private ImageView mPlay;
    private final SeekBar mSeekBar;
    private View mSyncView;
    private boolean mTracking;
    private boolean mHide;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnCompletionListener mOnCompletionListener;
    private final OnErrorListener mMyOnErrorListener = new OnErrorListener() {
        private boolean mNoError = true;
        private static final int MEDIA_ERROR_SYSTEM = -2147483648;

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
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
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mp, what, extra);
            }
            if (mNoError) {
                mNoError = false;
                return false;
            }
            return true;
        }
    };
    private final OnInfoListener mMyOnInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
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
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            }
            return false;
        }
    };
    private final OnCompletionListener mMyOnCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            removeCallbacks(mGetPositionTask);
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mp);
            }
        }
    };

    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public ControlView(Context context) {
        this(context, null);
    }

    public ControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        inflate(context, R.layout.view_control, this);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        assert mSeekBar != null;
        mSeekBar.setEnabled(false);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mProgress.setText(makeTimeText(progress));
                    postHideControlTask();
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
                    mPlay.setImageResource(R.drawable.ic_pause);
                }
            }
        });
        mProgress = (TextView) findViewById(R.id.textProgress);
        mDuration = (TextView) findViewById(R.id.textDuration);
        mPlay = (ImageView) findViewById(R.id.play);
        assert mPlay != null;
        mPlay.setImageResource(R.drawable.ic_play);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer == null) {
                    return;
                }
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPlay.setImageResource(R.drawable.ic_play);
                } else {
                    mMediaPlayer.start();
                    mPlay.setImageResource(R.drawable.ic_pause);
                }
                postHideControlTask();
            }
        });
    }

    private final Runnable mGetPositionTask = new Runnable() {
        @Override
        public void run() {
            try {
                int sleep = 1000;
                if (!mTracking && mMediaPlayer.isPlaying()) {
                    final int duration = mMediaPlayer.getDuration();
                    final int position = mMediaPlayer.getCurrentPosition();
                    if (mSeekBar.isEnabled() && duration >= position) {
                        mSeekBar.setProgress(position);
                        mProgress.setText(makeTimeText(position));
                    }
                    sleep = 1001 - position % 1000;
                }
                if (sleep > 1000) {
                    sleep = 1000;
                } else if (sleep < 100) {
                    sleep = 100;
                }
                removeCallbacks(this);
                postDelayed(this, sleep);
            } catch (final IllegalStateException ignored) {
            }
        }
    };

    private final Runnable mHideControlTask = new Runnable() {
        @Override
        public void run() {
            setVisibility(INVISIBLE);
        }
    };

    private void postHideControlTask() {
        if (mHide) {
            removeCallbacks(mHideControlTask);
            postDelayed(mHideControlTask, 5000);
        }
    }

    private String makeTimeText(long millisecond) {
        final long second = millisecond / 1000;
        final long minute = second / 60;
        final long hour = minute / 60;
        return String.format(Locale.US, "%01d:%02d:%02d", hour, minute % 60, second % 60);
    }

    public void setSyncVisibility(View view) {
        mSyncView = view;
    }

    public void setAutoHide(boolean enable) {
        mHide = enable;
        if (mHide) {
            postHideControlTask();
        } else {
            removeCallbacks(mHideControlTask);
            setVisibility(VISIBLE);
        }
    }

    public void setVisible() {
        setVisibility(VISIBLE);
        if (mHide) {
            postHideControlTask();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mSyncView != null) {
            mSyncView.setVisibility(visibility);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mGetPositionTask);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer = mp;
        mMediaPlayer.setOnErrorListener(mMyOnErrorListener);
        mMediaPlayer.setOnInfoListener(mMyOnInfoListener);
        mMediaPlayer.setOnCompletionListener(mMyOnCompletionListener);
        final int duration = mMediaPlayer.getDuration();
        if (duration != 0) {
            mSeekBar.setEnabled(true);
            mSeekBar.setMax(duration);
            mDuration.setText(makeTimeText(duration));
        }
        mMediaPlayer.setOnInfoListener(mMyOnInfoListener);
        post(mGetPositionTask);
        mPlay.setImageResource(R.drawable.ic_pause);
        mp.start();
    }
}
