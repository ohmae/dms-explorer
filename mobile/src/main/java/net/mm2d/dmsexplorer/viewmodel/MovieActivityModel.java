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
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.databinding.library.baseAdapters.BR;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.MoviePlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.log.EventLogger;
import net.mm2d.dmsexplorer.settings.RepeatMode;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.OnCompletionListener;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.SkipControlListener;
import net.mm2d.dmsexplorer.viewmodel.helper.MovieActivityPipHelper;
import net.mm2d.dmsexplorer.viewmodel.helper.MuteAlertHelper;
import net.mm2d.dmsexplorer.viewmodel.helper.PipHelpers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MovieActivityModel extends BaseObservable
        implements OnCompletionListener, SkipControlListener {
    public interface OnChangeContentListener {
        void onChangeContent();
    }

    private static final OnChangeContentListener ON_CHANGE_CONTENT_LISTENER = () -> {
    };

    @NonNull
    public final ControlPanelParam controlPanelParam;
    public final boolean canUsePictureInPicture = PipHelpers.isSupported();
    @ColorInt
    public final int background;

    @NonNull
    private String mTitle;
    @NonNull
    private ControlPanelModel mControlPanelModel;
    private int mRightNavigationSize;

    @NonNull
    private OnChangeContentListener mOnChangeContentListener = ON_CHANGE_CONTENT_LISTENER;
    @NonNull
    private RepeatMode mRepeatMode;
    @DrawableRes
    private int mRepeatIconId;
    @Nullable
    private Toast mToast;

    @NonNull
    private final BaseActivity mActivity;
    @NonNull
    private final VideoView mVideoView;
    @NonNull
    private final Repository mRepository;
    @NonNull
    private final MediaServerModel mServerModel;
    @NonNull
    private final Settings mSettings;
    @NonNull
    private final MovieActivityPipHelper mMovieActivityPipHelper;
    @NonNull
    private final MuteAlertHelper mMuteAlertHelper;

    private static final long TOO_SHORT_PLAY_TIME = 2000;
    private long mPlayStartTime;
    private boolean mFinishing;

    public MovieActivityModel(
            @NonNull final BaseActivity activity,
            @NonNull final VideoView videoView,
            @NonNull final Repository repository) {
        mActivity = activity;
        mVideoView = videoView;
        mRepository = repository;
        mServerModel = repository.getMediaServerModel();
        if (mServerModel == null) {
            throw new IllegalStateException();
        }
        mSettings = new Settings(activity);
        mRepeatMode = mSettings.getRepeatModeMovie();
        mRepeatIconId = mRepeatMode.getIconId();

        background = mSettings.isMovieUiBackgroundTransparent()
                ? Color.TRANSPARENT
                : ContextCompat.getColor(activity, R.color.translucent_control);
        controlPanelParam = new ControlPanelParam();
        controlPanelParam.setBackgroundColor(background);

        mMovieActivityPipHelper = PipHelpers.getMovieHelper(mActivity);
        mMovieActivityPipHelper.register();
        mMuteAlertHelper = new MuteAlertHelper(activity);
        final PlaybackTargetModel targetModel = mRepository.getPlaybackTargetModel();
        if (targetModel == null || targetModel.getUri() == Uri.EMPTY) {
            throw new IllegalStateException();
        }
        updateTargetModel();
    }

    public void updateTargetModel() {
        final PlaybackTargetModel targetModel = mRepository.getPlaybackTargetModel();
        if (targetModel == null || targetModel.getUri() == Uri.EMPTY) {
            finishAfterTransition();
            return;
        }
        mPlayStartTime = System.currentTimeMillis();
        mMuteAlertHelper.alertIfMuted();
        final PlayerModel playerModel = new MoviePlayerModel(mActivity, mVideoView);
        mControlPanelModel = new ControlPanelModel(mActivity, playerModel);
        mControlPanelModel.setRepeatMode(mRepeatMode);
        mControlPanelModel.setOnCompletionListener(this);
        mControlPanelModel.setSkipControlListener(this);
        mMovieActivityPipHelper.setControlPanelModel(mControlPanelModel);
        playerModel.setUri(targetModel.getUri(), null);
        mTitle = mSettings.shouldShowTitleInMovieUi()
                ? AribUtils.toDisplayableString(targetModel.getTitle())
                : "";

        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.controlPanelModel);
    }

    public void adjustPanel(@NonNull final Activity activity) {
        final Point size = DisplaySizeUtils.getNavigationBarArea(activity);
        setRightNavigationSize(size.x);
        controlPanelParam.setBottomPadding(size.y);
    }

    public void terminate() {
        mControlPanelModel.terminate();
        mMovieActivityPipHelper.unregister();
    }

    public void restoreSaveProgress(final int position) {
        mControlPanelModel.restoreSaveProgress(position);
    }

    public int getCurrentProgress() {
        return mControlPanelModel.getProgress();
    }

    public void setOnChangeContentListener(@Nullable final OnChangeContentListener listener) {
        mOnChangeContentListener = listener != null ? listener : ON_CHANGE_CONTENT_LISTENER;
    }

    public void onClickBack() {
        mActivity.navigateUpTo();
    }

    public void onClickRepeat() {
        mRepeatMode = mRepeatMode.next();
        mControlPanelModel.setRepeatMode(mRepeatMode);
        setRepeatIconId(mRepeatMode.getIconId());
        mSettings.setRepeatModeMovie(mRepeatMode);

        showRepeatToast();
    }

    public void onClickPictureInPicture() {
        mMovieActivityPipHelper.enterPictureInPictureMode(mVideoView);
    }

    private void showRepeatToast() {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toaster.show(mActivity, mRepeatMode.getMessageId());
    }

    @Bindable
    public int getRepeatIconId() {
        return mRepeatIconId;
    }

    public void setRepeatIconId(@DrawableRes final int id) {
        mRepeatIconId = id;
        notifyPropertyChanged(net.mm2d.dmsexplorer.BR.repeatIconId);
    }

    @NonNull
    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    @Bindable
    public ControlPanelModel getControlPanelModel() {
        return mControlPanelModel;
    }

    @Bindable
    public int getRightNavigationSize() {
        return mRightNavigationSize;
    }

    private void setRightNavigationSize(final int rightNavigationSize) {
        controlPanelParam.setMarginRight(rightNavigationSize);
        mRightNavigationSize = rightNavigationSize;
        notifyPropertyChanged(BR.rightNavigationSize);
    }

    private boolean isTooShortPlayTime() {
        final long playTime = System.currentTimeMillis() - mPlayStartTime;
        return !mControlPanelModel.isSkipped()
                && playTime < TOO_SHORT_PLAY_TIME;
    }

    @Override
    public void onCompletion() {
        mControlPanelModel.terminate();
        if (isTooShortPlayTime() || mControlPanelModel.hasError() || !selectNext()) {
            finishAfterTransition();
            return;
        }
        updateTargetModel();
        mOnChangeContentListener.onChangeContent();
        EventLogger.sendPlayContent(true);
    }

    @Override
    public void next() {
        mControlPanelModel.terminate();
        if (!selectNext()) {
            finishAfterTransition();
            return;
        }
        updateTargetModel();
        mOnChangeContentListener.onChangeContent();
        EventLogger.sendPlayContent(true);
    }

    @Override
    public void previous() {
        mControlPanelModel.terminate();
        if (!selectPrevious()) {
            finishAfterTransition();
            return;
        }
        updateTargetModel();
        mOnChangeContentListener.onChangeContent();
        EventLogger.sendPlayContent(true);
    }

    private void finishAfterTransition() {
        if (!mFinishing) {
            mFinishing = true;
            ActivityCompat.finishAfterTransition(mActivity);
        }
    }

    private boolean selectNext() {
        switch (mRepeatMode) {
            case PLAY_ONCE:
                return false;
            case SEQUENTIAL:
                return mServerModel.selectNextEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL);
            case REPEAT_ALL:
                return mServerModel.selectNextEntity(MediaServerModel.SCAN_MODE_LOOP);
            case REPEAT_ONE:
                return false;
        }
        return false;
    }

    private boolean selectPrevious() {
        switch (mRepeatMode) {
            case PLAY_ONCE:
                return false;
            case SEQUENTIAL:
                return mServerModel.selectPreviousEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL);
            case REPEAT_ALL:
                return mServerModel.selectPreviousEntity(MediaServerModel.SCAN_MODE_LOOP);
            case REPEAT_ONE:
                return false;
        }
        return false;
    }
}
