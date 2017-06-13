/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MediaRenderer.ActionCallback;
import net.mm2d.android.upnp.avt.TransportState;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.ChapterInfo;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MediaRendererModel implements PlayerModel {
    private static final int CHAPTER_MARGIN = (int) TimeUnit.SECONDS.toMillis(5);
    private static final int STOPPING_THRESHOLD = 5;
    private static final StatusListener STATUS_LISTENER = new StatusListenerAdapter();

    @NonNull
    private StatusListener mStatusListener = STATUS_LISTENER;
    private final MediaRenderer mMediaRenderer;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private int[] mChapterInfo;
    private boolean mPlaying;
    private int mProgress;
    private int mDuration;
    private boolean mStarted;
    private int mStoppingCount;

    @NonNull
    private final Runnable mGetPositionTask = new Runnable() {
        @Override
        public void run() {
            mMediaRenderer.getPositionInfo((success, result) ->
                    mHandler.post(() -> onGetPositionInfo(result)));
            mMediaRenderer.getTransportInfo((success, result) ->
                    mHandler.post(() -> onGetTransportInfo(result)));
        }
    };
    @NonNull
    private final ActionCallback mShowToastOnError = (success, result) -> {
        if (!success) {
            mHandler.post(this::onError);
        }
    };

    public MediaRendererModel(@NonNull Context context, @NonNull final MediaRenderer renderer) {
        mMediaRenderer = renderer;
    }

    @Override
    public String getName() {
        return mMediaRenderer.getFriendlyName();
    }

    @Override
    public boolean canPause() {
        return mMediaRenderer.isSupportPause();
    }

    @Override
    public void terminate() {
        if (!mStarted) {
            return;
        }
        mHandler.removeCallbacks(mGetPositionTask);
        mMediaRenderer.stop(null);
        mMediaRenderer.clearAVTransportURI(null);
        mMediaRenderer.unsubscribe();
        mStarted = false;
    }

    @Override
    public void setStatusListener(@NonNull final StatusListener listener) {
        mStatusListener = listener;
    }

    @Override
    public void setUri(@NonNull final Uri uri, @Nullable final Object metadata) {
        if (!(metadata instanceof CdsObject)) {
            throw new IllegalArgumentException();
        }
        mMediaRenderer.clearAVTransportURI(null);
        final CdsObject object = (CdsObject) metadata;
        mMediaRenderer.setAVTransportURI(object, uri.toString(), (success, result) -> {
            if (success) {
                mMediaRenderer.play(mShowToastOnError);
            } else {
                mHandler.post(this::onError);
            }
        });
        mStoppingCount = 0;
        mHandler.postDelayed(mGetPositionTask, 1000);
        ChapterInfo.get(object, this::setChapterInfo);
        mStarted = true;
    }

    @Override
    public void restoreSaveProgress(final int progress) {
        mProgress = progress;
    }

    @Override
    public int getProgress() {
        return mProgress;
    }

    @Override
    public int getDuration() {
        return mDuration;
    }

    @Override
    public boolean isPlaying() {
        return mPlaying;
    }

    @Override
    public void play() {
        mMediaRenderer.play(mShowToastOnError);
    }

    @Override
    public void pause() {
        mMediaRenderer.pause(mShowToastOnError);
    }

    @Override
    public void seekTo(final int position) {
        mMediaRenderer.seek(position, mShowToastOnError);
        mStoppingCount = 0;
        mHandler.removeCallbacks(mGetPositionTask);
        mHandler.postDelayed(mGetPositionTask, 1000);
    }

    @Override
    public boolean next() {
        if (mChapterInfo == null) {
            return false;
        }
        final int chapter = getCurrentChapter() + 1;
        if (chapter < mChapterInfo.length) {
            mMediaRenderer.seek(mChapterInfo[chapter], mShowToastOnError);
            return true;
        }
        return false;
    }

    @Override
    public boolean previous() {
        if (mChapterInfo == null) {
            return false;
        }
        int chapter = getCurrentChapter();
        if (chapter > 0 && mProgress - mChapterInfo[chapter] < CHAPTER_MARGIN) {
            chapter--;
        }
        if (chapter >= 0) {
            mMediaRenderer.seek(mChapterInfo[chapter], mShowToastOnError);
            return true;
        }
        return false;
    }

    private int getCurrentChapter() {
        if (mChapterInfo == null) {
            return 0;
        }
        final int progress = mProgress;
        for (int i = 0; i < mChapterInfo.length; i++) {
            if (progress < mChapterInfo[i]) {
                return i - 1;
            }
        }
        return mChapterInfo.length - 1;
    }

    private void onGetPositionInfo(final Map<String, String> result) {
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
        if (mDuration != duration) {
            mDuration = duration;
            mStatusListener.notifyDuration(duration);
        }
        if (mProgress != progress) {
            mProgress = progress;
            mStatusListener.notifyProgress(progress);
        }
        final long interval = 1000 - progress % 1000;
        mHandler.postDelayed(mGetPositionTask, interval);
    }

    private void onGetTransportInfo(final Map<String, String> result) {
        if (result == null) {
            return;
        }
        final TransportState state = MediaRenderer.getCurrentTransportState(result);
        final boolean playing = state == TransportState.PLAYING;
        if (mPlaying != playing) {
            mPlaying = playing;
            mStatusListener.notifyPlayingState(playing);
        }
        mStoppingCount = state == TransportState.STOPPED ? mStoppingCount + 1 : 0;
        if (mStoppingCount > STOPPING_THRESHOLD) {
            mHandler.removeCallbacks(mGetPositionTask);
            mStatusListener.onCompletion();
        }
    }

    private void setChapterInfo(@Nullable final int[] chapterInfo) {
        mChapterInfo = chapterInfo;
        mStatusListener.notifyChapterInfo(mChapterInfo);
    }

    private void onError() {
        mStatusListener.onError(0, 0);
    }
}
