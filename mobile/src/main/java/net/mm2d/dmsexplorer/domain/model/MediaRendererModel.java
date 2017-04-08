/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MediaRenderer.ActionCallback;
import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.ChapterInfo;
import net.mm2d.dmsexplorer.R;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MediaRendererModel {
    public interface ControlListener {
        void notifyPosition(int progress, int duration);

        void notifyPlayingState(boolean playing);

        void notifyChapterInfo(@Nullable List<Integer> chapterInfo);
    }

    private static final ControlListener CONTROL_LISTENER = new ControlListener() {
        @Override
        public void notifyPosition(final int progress, final int duration) {
        }

        @Override
        public void notifyPlayingState(final boolean playing) {
        }

        @Override
        public void notifyChapterInfo(@Nullable final List<Integer> chapterInfo) {
        }
    };

    private static final int CHAPTER_MARGIN = (int) TimeUnit.SECONDS.toMillis(5);
    private final Context mContext;
    private final MediaRenderer mMediaRenderer;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ControlListener mControlListener = CONTROL_LISTENER;
    @Nullable
    private List<Integer> mChapterInfo;
    private int mProgress;
    private boolean mStarted;

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

    public MediaRendererModel(@NonNull Context context, @NonNull final MediaRenderer renderer) {
        mContext = context;
        mMediaRenderer = renderer;
    }

    public MediaRenderer getMediaRenderer() {
        return mMediaRenderer;
    }

    public void initialize() {
    }

    public void terminate() {
        if (mStarted) {
            stop();
        }
    }

    public void setControlListener(ControlListener listener) {
        mControlListener = listener != null ? listener : CONTROL_LISTENER;
    }

    public void start(PlaybackTargetModel targetModel) {
        mMediaRenderer.clearAVTransportURI(null);
        final CdsObject object = targetModel.getCdsObject();
        final String uri = targetModel.getUri().toString();
        mMediaRenderer.setAVTransportURI(object, uri, (success, result) -> {
            if (success) {
                mMediaRenderer.play(mShowToastOnError);
            } else {
                mHandler.post(() -> showToast(R.string.toast_command_error_occurred));
            }
        });
        mHandler.postDelayed(mGetPositionTask, 1000);
        ChapterInfo.get(object, this::setChapterInfo);
        mStarted = true;
    }

    public void stop() {
        mHandler.removeCallbacks(mGetPositionTask);
        mMediaRenderer.stop(null);
        mMediaRenderer.clearAVTransportURI(null);
        mMediaRenderer.unsubscribe();
        mStarted = false;
    }

    public void play() {
        mMediaRenderer.play(mShowToastOnError);
    }

    public void pause() {
        mMediaRenderer.pause(mShowToastOnError);
    }

    public void seek(int progress) {
        mMediaRenderer.seek(progress, mShowToastOnError);
    }

    public void nextChapter() {
        final int chapter = getCurrentChapter() + 1;
        if (chapter < mChapterInfo.size()) {
            mMediaRenderer.seek(mChapterInfo.get(chapter), mShowToastOnError);
        }
    }

    public void previousChapter() {
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
        mProgress = progress;
        final long interval = 1000 - progress % 1000;
        mHandler.postDelayed(mGetPositionTask, interval);
        mControlListener.notifyPosition(progress, duration);
    }

    private void onGetTransportInfo(Map<String, String> result) {
        if (result == null) {
            return;
        }
        final boolean playing = "PLAYING".equals(MediaRenderer.getCurrentTransportState(result));
        mControlListener.notifyPlayingState(playing);
    }

    private void setChapterInfo(@Nullable final List<Integer> chapterInfo) {
        mChapterInfo = chapterInfo;
        mControlListener.notifyChapterInfo(mChapterInfo);
    }

    private void showToast(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
    }
}
