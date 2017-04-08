/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MediaRenderer.ActionCallback;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.ChapterInfo;
import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MediaRendererModel;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.view.adapter.ContentPropertyAdapter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class DmcActivityModel extends BaseObservable {
    private static final char EN_SPACE = 0x2002; // &ensp;
    public final String title;
    public final String subtitle;
    public final ContentPropertyAdapter propertyAdapter;
    public final int imageResource;
    public final Drawable progressDrawable;
    public final boolean isPlayControlEnabled;
    public final boolean isStillContents;
    public final OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                setProgressText(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(mTrackingCancel);
            mTracking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mMediaRenderer.seek(seekBar.getProgress(), mShowToastOnError);
            mHandler.postDelayed(mTrackingCancel, 1000);
        }
    };

    private String mProgressText = makeTimeText(0);
    private String mDurationText = makeTimeText(0);
    private boolean mPlaying;
    private boolean mPrepared;
    private int mDuration;
    private int mProgress;
    private boolean mSeekable;
    @Nullable
    private List<Integer> mChapterInfo;
    private boolean mChapterInfoEnabled;

    private static final int CHAPTER_MARGIN = (int) TimeUnit.SECONDS.toMillis(5);
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Context mContext;
    private final CdsObject mCdsObject;
    private final MediaRenderer mMediaRenderer;
    private boolean mTracking;
    private final PlaybackTargetModel mPlaybackTargetModel;
    private final MediaRendererModel mMediaRendererModel;

    @NonNull
    private final Runnable mTrackingCancel = new Runnable() {
        @Override
        public void run() {
            mTracking = false;
        }
    };
    @NonNull
    private final Runnable mGetPositionTask = new Runnable() {
        @Override
        public void run() {
            mMediaRenderer.getPositionInfo((success, result) -> onGetPositionInfo(result));
            mMediaRenderer.getTransportInfo((success, result) -> onGetTransportInfo(result));
        }
    };
    @NonNull
    private final ActionCallback mShowToastOnError = (success, result) -> {
        if (!success) {
            mHandler.post(() -> showToast(R.string.toast_command_error_occurred));
        }
    };


    private void onGetPositionInfo(Map<String, String> result) {
        if (result == null) {
            mHandler.postDelayed(mGetPositionTask, 1000);
            return;
        }
        final int duration = MediaRenderer.getDuration(result);
        final int progress = MediaRenderer.getProgress(result);
        if (duration < 0 || progress < 0) {
            mHandler.postDelayed(mGetPositionTask, 1000);
            return;
        }
        final long interval = 1000 - progress % 1000;
        mHandler.postDelayed(mGetPositionTask, interval);
        setDuration(duration);
        if (mTracking) {
            return;
        }
        setProgress(progress);
    }

    private void onGetTransportInfo(Map<String, String> result) {
        setPlaying("PLAYING".equals(MediaRenderer.getCurrentTransportState(result)));
    }

    public static DmcActivityModel create(@NonNull final Context context) {
        final Repository repository = Repository.getInstance();
        final MediaServerModel serverModel = repository.getMediaServerModel();
        final MediaRendererModel rendererModel = repository.getMediaRendererModel();
        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        if (serverModel == null || rendererModel == null
                || targetModel == null || targetModel.getUri() == null) {
            return null;
        }
        return new DmcActivityModel(context, targetModel, serverModel, rendererModel);
    }

    private DmcActivityModel(@NonNull final Context context,
                             @NonNull final PlaybackTargetModel targetModel,
                             @NonNull final MediaServerModel serverModel,
                             @NonNull final MediaRendererModel rendererModel) {
        mContext = context;
        mPlaybackTargetModel = targetModel;
        mMediaRendererModel = rendererModel;
        mCdsObject = targetModel.getCdsObject();
        title = AribUtils.toDisplayableString(mCdsObject.getTitle());
        mMediaRenderer = rendererModel.getMediaRenderer();
        isStillContents = mCdsObject.getType() == CdsObject.TYPE_IMAGE;
        isPlayControlEnabled = !isStillContents && mMediaRenderer.isSupportPause();
        subtitle = mMediaRenderer.getFriendlyName()
                + "  ←  "
                + serverModel.getMediaServer().getFriendlyName();
        propertyAdapter = new ContentPropertyAdapter(context, mCdsObject);
        imageResource = getImageResource(mCdsObject);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            progressDrawable = context.getDrawable(R.drawable.seekbar_track);
        } else {
            progressDrawable = null;
        }
    }

    public void initialize() {
        start(mPlaybackTargetModel.getUri().toString(), mCdsObject);
        if (mCdsObject.getType() == CdsObject.TYPE_IMAGE) {
            return;
        }
        mHandler.postDelayed(mGetPositionTask, 1000);
        ChapterInfo.get(mCdsObject, this::setChapterInfo);
    }

    private void start(@NonNull final String uri, @NonNull final CdsObject object) {
        mMediaRenderer.setAVTransportURI(object, uri, (success, result) -> {
            if (success) {
                mMediaRenderer.play(mShowToastOnError);
            } else {
                mHandler.post(() -> {
                    showToast(R.string.toast_command_error_occurred);
                    ((Activity) mContext).finish();
                });
            }
        });
    }


    public void terminate() {
        mHandler.removeCallbacks(mGetPositionTask);
        if (mMediaRenderer != null) {
            mMediaRenderer.stop(null);
            mMediaRenderer.clearAVTransportURI(null);
            mMediaRenderer.unsubscribe();
        }
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

    @Bindable
    public String getProgressText() {
        return mProgressText;
    }

    public void setProgressText(final int progress) {
        mProgressText = makeTimeText(progress);
        notifyPropertyChanged(BR.progressText);
    }

    @Bindable
    public String getDurationText() {
        return mDurationText;
    }

    private void setDurationText(final int duration) {
        mDurationText = makeTimeText(duration);
        notifyPropertyChanged(BR.durationText);
    }

    @Bindable
    public boolean isPlaying() {
        return mPlaying;
    }

    public void setPlaying(final boolean playing) {
        if (mPlaying == playing) {
            return;
        }
        mPlaying = playing;
        notifyPropertyChanged(BR.playing);
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
    public List<Integer> getChapterInfo() {
        return mChapterInfo;
    }

    public void setChapterInfo(@Nullable final List<Integer> chapterInfo) {
        mChapterInfo = chapterInfo;
        notifyPropertyChanged(BR.chapterInfo);
        setChapterInfoEnabled();
        mHandler.post(() -> {
            final int count = propertyAdapter.getItemCount();
            propertyAdapter.addEntry(mContext.getString(R.string.prop_chapter_info),
                    makeChapterString(chapterInfo));
            propertyAdapter.notifyItemInserted(count);
        });
    }

    @Bindable
    public boolean isChapterInfoEnabled() {
        return mChapterInfoEnabled;
    }

    public void setChapterInfoEnabled() {
        mChapterInfoEnabled = (mDuration != 0 && mChapterInfo != null);
        notifyPropertyChanged(BR.chapterInfoEnabled);
    }

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

    private static String makeTimeText(int millisecond) {
        final long second = (millisecond / 1000) % 60;
        final long minute = (millisecond / 60000) % 60;
        final long hour = millisecond / 3600000;
        return String.format(Locale.US, "%01d:%02d:%02d", hour, minute, second);
    }

    @NonNull
    private static String makeChapterString(List<Integer> chapterInfo) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chapterInfo.size(); i++) {
            if (sb.length() != 0) {
                sb.append("\n");
            }
            if (i < 9) {
                sb.append(EN_SPACE);
            }
            sb.append(String.valueOf(i + 1));
            sb.append(" : ");
            final int chapter = chapterInfo.get(i);
            sb.append(makeTimeText(chapter));
        }
        return sb.toString();
    }

    private void showToast(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
    }

    public void onClickPlay(View view) {
        if (mPlaying) {
            mMediaRenderer.pause(mShowToastOnError);
        } else {
            mMediaRenderer.play(mShowToastOnError);
        }
    }

    public void onClickNext(View view) {
        final int chapter = getCurrentChapter() + 1;
        if (chapter < mChapterInfo.size()) {
            mMediaRenderer.seek(mChapterInfo.get(chapter), mShowToastOnError);
        }
    }

    public void onClickPrevious(View view) {
        int chapter = getCurrentChapter();
        if (chapter > 0 && mProgress - mChapterInfo.get(chapter) < CHAPTER_MARGIN) {
            chapter--;
        }
        if (chapter >= 0) {
            mMediaRenderer.seek(mChapterInfo.get(chapter), mShowToastOnError);
        }
    }

    private int getCurrentChapter() {
        final int progress = mProgress;
        for (int i = 0; i < mChapterInfo.size(); i++) {
            if (progress < mChapterInfo.get(i)) {
                return i - 1;
            }
        }
        return mChapterInfo.size() - 1;
    }
}
