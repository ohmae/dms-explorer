/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import net.mm2d.dmsexplorer.BR;

import java.util.Locale;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ControlViewModel extends BaseObservable {
    private String mProgressText = makeTimeText(0);
    private String mDurationText = makeTimeText(0);
    private boolean mPlaying;
    private boolean mPlayButtonEnabled;
    private int mSeekBarMax;
    private int mSeekBarProgress;
    private boolean mSeekBarEnabled;

    public void setProgress(final int progress) {
        setProgressText(progress);
        setSeekBarProgress(progress);
    }

    public void setDuration(final int duration) {
        if (duration > 0) {
            setSeekBarEnabled(true);
        }
        setDurationText(duration);
        setSeekBarMax(duration);
        setPlayButtonEnabled(true);
    }

    @Bindable
    public String getProgressText() {
        return mProgressText;
    }

    public void setProgressText(final long progress) {
        mProgressText = makeTimeText(progress);
        notifyPropertyChanged(BR.progressText);
    }

    @Bindable
    public String getDurationText() {
        return mDurationText;
    }

    private void setDurationText(final long duration) {
        mDurationText = makeTimeText(duration);
        notifyPropertyChanged(BR.durationText);
    }

    @Bindable
    public boolean isPlaying() {
        return mPlaying;
    }

    public void setPlaying(final boolean playing) {
        mPlaying = playing;
        notifyPropertyChanged(BR.playing);
    }

    @Bindable
    public boolean isPlayButtonEnabled() {
        return mPlayButtonEnabled;
    }

    private void setPlayButtonEnabled(final boolean playButtonEnabled) {
        mPlayButtonEnabled = playButtonEnabled;
        notifyPropertyChanged(BR.playButtonEnabled);
    }

    @Bindable
    public int getSeekBarMax() {
        return mSeekBarMax;
    }

    private void setSeekBarMax(final int seekBarMax) {
        mSeekBarMax = seekBarMax;
        notifyPropertyChanged(BR.seekBarMax);
    }

    @Bindable
    public int getSeekBarProgress() {
        return mSeekBarProgress;
    }

    private void setSeekBarProgress(final int seekBarProgress) {
        mSeekBarProgress = seekBarProgress;
        notifyPropertyChanged(BR.seekBarProgress);
    }

    @Bindable
    public boolean isSeekBarEnabled() {
        return mSeekBarEnabled;
    }

    private void setSeekBarEnabled(final boolean seekBarEnabled) {
        mSeekBarEnabled = seekBarEnabled;
        notifyPropertyChanged(BR.seekBarEnabled);
    }

    private static String makeTimeText(long millisecond) {
        final long second = millisecond / 1000;
        final long minute = second / 60;
        final long hour = minute / 60;
        return String.format(Locale.US, "%01d:%02d:%02d", hour, minute % 60, second % 60);
    }
}
