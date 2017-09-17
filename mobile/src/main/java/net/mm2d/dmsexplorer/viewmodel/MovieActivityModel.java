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
import android.graphics.Point;
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
import net.mm2d.dmsexplorer.EventLogger;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.MoviePlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.settings.RepeatMode;
import net.mm2d.dmsexplorer.settings.Settings;
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
    private final Activity mActivity;
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

    public MovieActivityModel(
            @NonNull final Activity activity,
            @NonNull final VideoView videoView,
            @NonNull final Repository repository) {
        mActivity = activity;
        mVideoView = videoView;
        mRepository = repository;
        mServerModel = repository.getMediaServerModel();
        if (mServerModel == null) {
            throw new IllegalStateException();
        }
        mSettings = new Settings();
        mRepeatMode = mSettings.getRepeatModeMovie();
        mRepeatIconId = mRepeatMode.getIconId();

        final int color = ContextCompat.getColor(activity, R.color.translucent_control);
        controlPanelParam = new ControlPanelParam();
        controlPanelParam.setBackgroundColor(color);
        mMovieActivityPipHelper = PipHelpers.getMovieHelper(mActivity);
        mMovieActivityPipHelper.register();
        mMuteAlertHelper = new MuteAlertHelper(activity);
        updateTargetModel();
    }

    private void updateTargetModel() {
        final PlaybackTargetModel targetModel = mRepository.getPlaybackTargetModel();
        if (targetModel == null) {
            throw new IllegalStateException();
        }
        mMuteAlertHelper.alertIfMuted();
        final PlayerModel playerModel = new MoviePlayerModel(mActivity, mVideoView);
        mControlPanelModel = new ControlPanelModel(mActivity, playerModel);
        mControlPanelModel.setRepeatMode(mRepeatMode);
        mControlPanelModel.setOnCompletionListener(this);
        mControlPanelModel.setSkipControlListener(this);
        mMovieActivityPipHelper.setControlPanelModel(mControlPanelModel);
        playerModel.setUri(targetModel.getUri(), null);
        mTitle = AribUtils.toDisplayableString(targetModel.getTitle());

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
        ActivityCompat.finishAfterTransition(mActivity);
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
        mToast = Toaster.showLong(mActivity, mRepeatMode.getMessageId());
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

    @Override
    public void onCompletion() {
        mControlPanelModel.terminate();
        if (!selectNext()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
        mOnChangeContentListener.onChangeContent();
        EventLogger.sendPlayContent();
    }

    @Override
    public void next() {
        mControlPanelModel.terminate();
        if (!selectNext()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
        mOnChangeContentListener.onChangeContent();
        EventLogger.sendPlayContent();
    }

    @Override
    public void previous() {
        mControlPanelModel.terminate();
        if (!selectPrevious()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
        mOnChangeContentListener.onChangeContent();
        EventLogger.sendPlayContent();
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
