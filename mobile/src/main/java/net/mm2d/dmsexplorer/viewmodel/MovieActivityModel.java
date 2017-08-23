/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.viewmodel;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Rational;
import android.view.View;
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
import net.mm2d.dmsexplorer.settings.RepeatMode;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.view.MovieActivity;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.OnCompletionListener;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.SkipControlListener;
import net.mm2d.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public final boolean isSupportPictureInPicture = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

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
    private PlayerModel mPlayerModel;

    public MovieActivityModel(
            @NonNull final Activity activity,
            @NonNull final VideoView videoView,
            @NonNull final Repository repository) {
        mActivity = activity;
        mVideoView = videoView;
        mRepository = repository;
        mServerModel = repository.getMediaServerModel();
        mSettings = new Settings(activity);
        mRepeatMode = mSettings.getRepeatModeMovie();
        mRepeatIconId = mRepeatMode.getIconId();

        final int color = ContextCompat.getColor(activity, R.color.translucent_control);
        controlPanelParam = new ControlPanelParam();
        controlPanelParam.setBackgroundColor(color);
        updateTargetModel();
    }

    private void updateTargetModel() {
        final PlaybackTargetModel targetModel = mRepository.getPlaybackTargetModel();
        if (targetModel == null) {
            throw new IllegalStateException();
        }
        mPlayerModel = new MoviePlayerModel(mVideoView);
        mControlPanelModel = new ControlPanelModel(mActivity, mPlayerModel);
        mControlPanelModel.setRepeatMode(mRepeatMode);
        mControlPanelModel.setOnCompletionListener(this);
        mControlPanelModel.setSkipControlListener(this);
        mPlayerModel.setUri(targetModel.getUri(), null);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder()
                    .setActions(makeActions());
            final Rect rect = makeViewRect(mVideoView);
            Log.e(rect.toString());
            if (rect.width() > 0 && rect.height() > 0) {
                builder.setAspectRatio(new Rational(rect.width(), rect.height()))
                        .setSourceRectHint(rect);
            }
            try {
                mActivity.enterPictureInPictureMode(builder.build());
            } catch (final Exception e) {
                Log.w(e);
            }
        }
    }

    @NonNull
    private Rect makeViewRect(final View v) {
        final Rect rect = new Rect();
        v.getGlobalVisibleRect(rect);
        return rect;
    }

    @TargetApi(VERSION_CODES.O)
    private List<RemoteAction> makeActions() {
        final int max = mActivity.getMaxNumPictureInPictureActions();
        if (max <= 0) {
            return Collections.emptyList();
        }
        if (max >= 3) {
            return Arrays.asList(
                    makePreviousAction(),
                    makePlayAction(),
                    makeNextAction()
            );
        }
        return Arrays.asList(makePlayAction());
    }

    @TargetApi(VERSION_CODES.O)
    @NonNull
    private RemoteAction makePlayAction() {
        return new RemoteAction(
                makeIcon(R.drawable.ic_play),
                getString(R.string.action_play_title),
                getString(R.string.action_play_description),
                MovieActivity.makePlayPendingIntent(mActivity));
    }

    @TargetApi(VERSION_CODES.O)
    @NonNull
    private RemoteAction makeNextAction() {
        return new RemoteAction(
                makeIcon(R.drawable.ic_skip_next),
                getString(R.string.action_next_title),
                getString(R.string.action_next_description),
                MovieActivity.makeNextPendingIntent(mActivity));
    }

    @TargetApi(VERSION_CODES.O)
    @NonNull
    private RemoteAction makePreviousAction() {
        return new RemoteAction(
                makeIcon(R.drawable.ic_skip_previous),
                getString(R.string.action_previous_title),
                getString(R.string.action_previous_description),
                MovieActivity.makePreviousPendingIntent(mActivity));
    }

    @NonNull
    private String getString(@StringRes final int resId) {
        return mActivity.getResources().getText(resId, "").toString();
    }

    @TargetApi(VERSION_CODES.M)
    private Icon makeIcon(@DrawableRes final int resId) {
        return Icon.createWithResource(mActivity, resId);
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
    }

    private boolean selectNext() {
        switch (mRepeatMode) {
            case PLAY_ONCE:
                return false;
            case SEQUENTIAL:
                return mServerModel.selectNextObject(MediaServerModel.SCAN_MODE_SEQUENTIAL);
            case REPEAT_ALL:
                return mServerModel.selectNextObject(MediaServerModel.SCAN_MODE_LOOP);
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
                return mServerModel.selectPreviousObject(MediaServerModel.SCAN_MODE_SEQUENTIAL);
            case REPEAT_ALL:
                return mServerModel.selectPreviousObject(MediaServerModel.SCAN_MODE_LOOP);
            case REPEAT_ONE:
                return false;
        }
        return false;
    }
}
