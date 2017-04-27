/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener;
import net.mm2d.dmsexplorer.view.view.ScrubBar;
import net.mm2d.dmsexplorer.view.view.ScrubBar.IntAccuracy;
import net.mm2d.dmsexplorer.view.view.ScrubBar.ScrubBarListener;

import java.util.Locale;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ControlPanelModel extends BaseObservable implements StatusListener {
    interface OnCompletionListener {
        void onCompletion();
    }

    private static final OnCompletionListener ON_COMPLETION_LISTENER = () -> {
    };

    private static String makeTimeText(int millisecond) {
        final long second = (millisecond / 1000) % 60;
        final long minute = (millisecond / 60000) % 60;
        final long hour = millisecond / 3600000;
        return String.format(Locale.US, "%01d:%02d:%02d", hour, minute, second);
    }

    @NonNull
    private String mProgressText = makeTimeText(0);
    @NonNull
    private String mDurationText = makeTimeText(0);
    private boolean mPlaying;
    private boolean mPrepared;
    private int mDuration;
    private int mProgress;
    private boolean mSeekable;
    private int mPlayButtonResId = R.drawable.ic_play;
    @NonNull
    private String mScrubText = "";
    private boolean mSkippable;

    private boolean mTracking;
    @NonNull
    private final Context mContext;
    @NonNull
    private final PlayerModel mPlayerModel;
    @NonNull
    private OnCompletionListener mOnCompletionListener = ON_COMPLETION_LISTENER;

    ControlPanelModel(@NonNull Context context, @NonNull PlayerModel playerModel) {
        mContext = context;
        mPlayerModel = playerModel;
        mPlayerModel.setStatusListener(this);
    }

    void terminate() {
        mPlayerModel.terminate();
    }

    void restoreSaveProgress(final int position) {
        mPlayerModel.restoreSaveProgress(position);
    }

    void setOnCompletionListener(@Nullable final OnCompletionListener listener) {
        mOnCompletionListener = listener != null ? listener : ON_COMPLETION_LISTENER;
    }

    public final ScrubBarListener seekBarListener = new ScrubBarListener() {
        @Override
        public void onProgressChanged(final ScrubBar seekBar, final int progress, final boolean fromUser) {
            if (fromUser) {
                setProgressText(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(final ScrubBar seekBar) {
            mTracking = true;
        }

        @Override
        public void onStopTrackingTouch(final ScrubBar seekBar) {
            mTracking = false;
            mPlayerModel.seekTo(seekBar.getProgress());
            setScrubText("");
        }

        @Override
        public void onAccuracyChanged(final ScrubBar seekBar, @IntAccuracy final int accuracy) {
            setScrubText(getScrubText(accuracy));
        }
    };

    public void onClickPlay() {
        final boolean playing = mPlayerModel.isPlaying();
        if (playing) {
            mPlayerModel.pause();
        } else {
            mPlayerModel.play();
        }
        setPlaying(!playing);
    }

    public void onClickNext() {
    }

    public void onClickPrevious() {
    }

    @Bindable
    public int getProgress() {
        return mProgress;
    }

    private void setProgress(final int progress) {
        if (mTracking) {
            return;
        }
        setProgressText(progress);
        mProgress = progress;
        notifyPropertyChanged(BR.progress);
    }

    @Bindable
    public int getDuration() {
        return mDuration;
    }

    private void setDuration(final int duration) {
        mDuration = duration;
        notifyPropertyChanged(BR.duration);
        if (duration > 0) {
            setSeekable(true);
        }
        setDurationText(duration);
        setPrepared(true);
    }

    @NonNull
    @Bindable
    public String getProgressText() {
        return mProgressText;
    }

    private void setProgressText(final int progress) {
        mProgressText = makeTimeText(progress);
        notifyPropertyChanged(BR.progressText);
    }

    @NonNull
    @Bindable
    public String getDurationText() {
        return mDurationText;
    }

    private void setDurationText(final int duration) {
        mDurationText = makeTimeText(duration);
        notifyPropertyChanged(BR.durationText);
    }

    private void setPlaying(final boolean playing) {
        if (mPlaying == playing) {
            return;
        }
        mPlaying = playing;
        setPlayButtonResId(playing ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    @Bindable
    public String getScrubText() {
        return mScrubText;
    }

    private String getScrubText(final int accuracy) {
        switch (accuracy) {
            case ScrubBar.ACCURACY_NORMAL:
                return mContext.getString(R.string.seek_bar_scrub_normal);
            case ScrubBar.ACCURACY_HALF:
                return mContext.getString(R.string.seek_bar_scrub_half);
            case ScrubBar.ACCURACY_QUARTER:
                return mContext.getString(R.string.seek_bar_scrub_quarter);
            default:
                return "";
        }
    }

    private void setScrubText(final String scrubText) {
        mScrubText = scrubText;
        notifyPropertyChanged(BR.scrubText);
    }

    @Bindable
    public int getPlayButtonResId() {
        return mPlayButtonResId;
    }

    private void setPlayButtonResId(final int playButtonResId) {
        mPlayButtonResId = playButtonResId;
        notifyPropertyChanged(BR.playButtonResId);
    }

    @Bindable
    public boolean isPrepared() {
        return mPrepared;
    }

    private void setPrepared(final boolean prepared) {
        mPrepared = prepared;
        notifyPropertyChanged(BR.prepared);
    }

    @Bindable
    public boolean isSeekable() {
        return mSeekable;
    }

    private void setSeekable(final boolean seekable) {
        mSeekable = seekable;
        notifyPropertyChanged(BR.seekable);
    }

    @Bindable
    public boolean isSkippable() {
        return mSkippable;
    }

    public void setSkippable(final boolean skippable) {
        mSkippable = skippable;
        notifyPropertyChanged(BR.skippable);
    }

    @Override
    public void notifyDuration(final int duration) {
        setDuration(duration);
    }

    @Override
    public void notifyProgress(final int progress) {
        setProgress(progress);
    }

    @Override
    public void notifyPlayingState(final boolean playing) {
        setPlaying(playing);
    }

    @Override
    public void notifyChapterInfo(@Nullable final int[] chapterInfo) {
    }

    @Override
    public boolean onError(final int what, final int extra) {
        return false;
    }

    @Override
    public boolean onInfo(final int what, final int extra) {
        return false;
    }

    @Override
    public void onCompletion() {
        mOnCompletionListener.onCompletion();
    }
}
