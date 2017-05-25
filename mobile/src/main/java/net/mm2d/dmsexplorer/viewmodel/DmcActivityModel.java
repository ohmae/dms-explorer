/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.app.Activity;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel.StatusListener;
import net.mm2d.dmsexplorer.view.adapter.ContentPropertyAdapter;
import net.mm2d.dmsexplorer.view.view.ScrubBar;
import net.mm2d.dmsexplorer.view.view.ScrubBar.Accuracy;
import net.mm2d.dmsexplorer.view.view.ScrubBar.ScrubBarListener;

import java.util.Locale;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DmcActivityModel extends BaseObservable implements StatusListener {
    private static final long TRACKING_DELAY = 1000L;
    private static final char EN_SPACE = 0x2002; // &ensp;
    @NonNull
    public final String title;
    @NonNull
    public final String subtitle;
    @NonNull
    public final ContentPropertyAdapter propertyAdapter;
    @DrawableRes
    public final int imageResource;
    public final boolean isPlayControlEnabled;
    public final boolean isStillContents;
    @NonNull
    public final ScrubBarListener seekBarListener;

    @NonNull
    private String mProgressText = makeTimeText(0);
    @NonNull
    private String mDurationText = makeTimeText(0);
    private boolean mPlaying;
    private boolean mPrepared;
    private int mDuration;
    private int mProgress;
    private boolean mSeekable;
    private int mPlayButtonResId;
    @NonNull
    private String mScrubText = "";
    @Nullable
    private int[] mChapterInfo;
    private boolean mChapterInfoEnabled;

    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final Activity mActivity;
    @NonNull
    private final PlaybackTargetModel mTargetModel;
    @NonNull
    private final PlayerModel mRendererModel;
    private boolean mTracking;

    @NonNull
    private final Runnable mTrackingCancel;

    public DmcActivityModel(@NonNull final Activity activity,
                            @NonNull final Repository repository) {
        mActivity = activity;
        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        final PlayerModel playerModel = repository.getMediaRendererModel();
        if (playerModel == null || targetModel == null || targetModel.getUri() == null) {
            throw new IllegalStateException();
        }
        mTargetModel = targetModel;
        mRendererModel = playerModel;
        mRendererModel.setStatusListener(this);
        mPlayButtonResId = R.drawable.ic_play;

        final CdsObject cdsObject = mTargetModel.getCdsObject();
        title = AribUtils.toDisplayableString(cdsObject.getTitle());
        isStillContents = cdsObject.getType() == CdsObject.TYPE_IMAGE;
        isPlayControlEnabled = !isStillContents && mRendererModel.canPause();
        final MediaServerModel serverModel = repository.getMediaServerModel();
        subtitle = mRendererModel.getName()
                + "  ←  "
                + serverModel.getMediaServer().getFriendlyName();
        propertyAdapter = new ContentPropertyAdapter(mActivity, cdsObject);
        imageResource = getImageResource(cdsObject);

        mTrackingCancel = () -> mTracking = false;
        seekBarListener = new ScrubBarListener() {
            @Override
            public void onProgressChanged(ScrubBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setProgressText(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(ScrubBar seekBar) {
                mHandler.removeCallbacks(mTrackingCancel);
                mTracking = true;
            }

            @Override
            public void onStopTrackingTouch(ScrubBar seekBar) {
                mRendererModel.seekTo(seekBar.getProgress());
                mHandler.postDelayed(mTrackingCancel, TRACKING_DELAY);
                setScrubText("");
            }

            @Override
            public void onAccuracyChanged(final ScrubBar seekBar, @Accuracy final int accuracy) {
                setScrubText(getAccuracyText(accuracy));
            }
        };
    }

    public void initialize() {
        final Uri uri = mTargetModel.getUri();
        final CdsObject object = mTargetModel.getCdsObject();
        mRendererModel.setUri(uri, object);
    }

    public void terminate() {
        mRendererModel.terminate();
    }

    @Bindable
    public int getProgress() {
        return mProgress;
    }

    public void setProgress(final int progress) {
        setProgressText(progress);
        mProgress = progress;
        notifyPropertyChanged(BR.progress);
    }

    @Bindable
    public int getDuration() {
        return mDuration;
    }

    public void setDuration(final int duration) {
        mDuration = duration;
        notifyPropertyChanged(BR.duration);
        if (duration > 0) {
            setSeekable(true);
        }
        setDurationText(duration);
        setPrepared(true);
        setChapterInfoEnabled();
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

    @NonNull
    @Bindable
    public String getScrubText() {
        return mScrubText;
    }

    private String getAccuracyText(final int accuracy) {
        switch (accuracy) {
            case ScrubBar.ACCURACY_NORMAL:
                return mActivity.getString(R.string.seek_bar_scrub_normal);
            case ScrubBar.ACCURACY_HALF:
                return mActivity.getString(R.string.seek_bar_scrub_half);
            case ScrubBar.ACCURACY_QUARTER:
                return mActivity.getString(R.string.seek_bar_scrub_quarter);
            default:
                return "";
        }
    }

    private void setScrubText(@NonNull final String scrubText) {
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
    @Nullable
    public int[] getChapterInfo() {
        return mChapterInfo;
    }

    private void setChapterInfo(@Nullable final int[] chapterInfo) {
        mChapterInfo = chapterInfo;
        notifyPropertyChanged(BR.chapterInfo);
        setChapterInfoEnabled();
        if (chapterInfo == null) {
            return;
        }
        mHandler.post(() -> {
            final int count = propertyAdapter.getItemCount();
            propertyAdapter.addEntry(mActivity.getString(R.string.prop_chapter_info),
                    makeChapterString(chapterInfo));
            propertyAdapter.notifyItemInserted(count);
        });
    }

    @Bindable
    public boolean isChapterInfoEnabled() {
        return mChapterInfoEnabled;
    }

    private void setChapterInfoEnabled() {
        mChapterInfoEnabled = (mDuration != 0 && mChapterInfo != null);
        notifyPropertyChanged(BR.chapterInfoEnabled);
    }

    @DrawableRes
    private static int getImageResource(@NonNull final CdsObject object) {
        switch (object.getType()) {
            case CdsObject.TYPE_VIDEO:
                return R.drawable.ic_movie;
            case CdsObject.TYPE_AUDIO:
                return R.drawable.ic_music;
            case CdsObject.TYPE_IMAGE:
                return R.drawable.ic_image;
        }
        return 0;
    }

    private static String makeTimeText(final int millisecond) {
        final long second = (millisecond / 1000) % 60;
        final long minute = (millisecond / 60000) % 60;
        final long hour = millisecond / 3600000;
        return String.format(Locale.US, "%01d:%02d:%02d", hour, minute, second);
    }

    @NonNull
    private static String makeChapterString(@NonNull final int[] chapterInfo) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chapterInfo.length; i++) {
            if (sb.length() != 0) {
                sb.append("\n");
            }
            if (i < 9) {
                sb.append(EN_SPACE);
            }
            sb.append(String.valueOf(i + 1));
            sb.append(" : ");
            final int chapter = chapterInfo[i];
            sb.append(makeTimeText(chapter));
        }
        return sb.toString();
    }

    public void onClickPlay() {
        if (mPlaying) {
            mRendererModel.pause();
        } else {
            mRendererModel.play();
        }
    }

    public void onClickNext() {
        mRendererModel.next();
    }

    public void onClickPrevious() {
        mRendererModel.previous();
    }

    @Override
    public void notifyDuration(final int duration) {
        setDuration(duration);
    }

    @Override
    public void notifyProgress(final int progress) {
        if (!mTracking) {
            setProgress(progress);
        }
    }

    @Override
    public void notifyPlayingState(final boolean playing) {
        setPlaying(playing);
    }

    @Override
    public void notifyChapterInfo(@Nullable final int[] chapterInfo) {
        setChapterInfo(chapterInfo);
    }

    @Override
    public boolean onError(final int what, final int extra) {
        Toaster.showLong(mActivity, R.string.toast_command_error_occurred);
        return false;
    }

    @Override
    public boolean onInfo(final int what, final int extra) {
        return false;
    }

    @Override
    public void onCompletion() {
        ActivityCompat.finishAfterTransition(mActivity);
    }
}
