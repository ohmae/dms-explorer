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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.EventLogger;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.MusicPlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.settings.RepeatMode;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.Downloader;
import net.mm2d.dmsexplorer.util.ThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.PropertyAdapter;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.OnCompletionListener;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.SkipControlListener;
import net.mm2d.dmsexplorer.viewmodel.helper.MuteAlertHelper;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MusicActivityModel extends BaseObservable
        implements OnCompletionListener, SkipControlListener {
    @NonNull
    public final ControlPanelParam controlPanelParam;

    @NonNull
    private String mTitle;
    private int mAccentColor;
    @NonNull
    private ControlPanelModel mControlPanelModel;
    @NonNull
    private PropertyAdapter mPropertyAdapter;
    @Nullable
    private byte[] mImageBinary;

    @NonNull
    private RepeatMode mRepeatMode;
    @DrawableRes
    private int mRepeatIconId;
    @Nullable
    private Toast mToast;

    @NonNull
    private final Activity mActivity;
    @NonNull
    private final Repository mRepository;
    @NonNull
    private final MediaServerModel mServerModel;
    @NonNull
    private final Settings mSettings;
    @NonNull
    private final MuteAlertHelper mMuteAlertHelper;

    public MusicActivityModel(
            @NonNull final Activity activity,
            @NonNull final Repository repository) {
        mActivity = activity;
        mRepository = repository;
        mServerModel = repository.getMediaServerModel();
        mSettings = new Settings(activity);
        mRepeatMode = mSettings.getRepeatModeMusic();
        mRepeatIconId = mRepeatMode.getIconId();

        controlPanelParam = new ControlPanelParam();
        mMuteAlertHelper = new MuteAlertHelper(activity);
        updateTargetModel();
    }

    private void updateTargetModel() {
        final PlaybackTargetModel targetModel = mRepository.getPlaybackTargetModel();
        if (targetModel == null || targetModel.getUri() == Uri.EMPTY) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        mMuteAlertHelper.alertIfMuted();
        final PlayerModel playerModel = new MusicPlayerModel(mActivity);
        mControlPanelModel = new ControlPanelModel(mActivity, playerModel);
        mControlPanelModel.setRepeatMode(mRepeatMode);
        mControlPanelModel.setOnCompletionListener(this);
        mControlPanelModel.setSkipControlListener(this);
        playerModel.setUri(targetModel.getUri(), null);

        mTitle = AribUtils.toDisplayableString(targetModel.getTitle());
        mAccentColor = ThemeUtils.getDeepColor(mTitle);
        mPropertyAdapter = PropertyAdapter.ofContent(mActivity, targetModel.getContentEntity());
        mRepository.getThemeModel().setThemeColor(mActivity, mAccentColor, 0);

        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.accentColor);
        notifyPropertyChanged(BR.propertyAdapter);
        notifyPropertyChanged(BR.controlPanelModel);

        controlPanelParam.setBackgroundColor(mAccentColor);

        loadArt(targetModel.getContentEntity().getArtUri());
    }

    private void loadArt(@NonNull final Uri uri) {
        setImageBinary(null);
        if (uri == Uri.EMPTY) {
            return;
        }
        Downloader.create(uri.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setImageBinary);
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

    public void onClickRepeat() {
        mRepeatMode = mRepeatMode.next();
        mControlPanelModel.setRepeatMode(mRepeatMode);
        setRepeatIconId(mRepeatMode.getIconId());
        mSettings.setRepeatModeMusic(mRepeatMode);

        showRepeatToast();
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
        notifyPropertyChanged(BR.repeatIconId);
    }

    @Nullable
    @Bindable
    public byte[] getImageBinary() {
        return mImageBinary;
    }

    public void setImageBinary(@Nullable final byte[] imageBinary) {
        mImageBinary = imageBinary;
        notifyPropertyChanged(BR.imageBinary);
    }

    @NonNull
    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Bindable
    public int getAccentColor() {
        return mAccentColor;
    }

    @NonNull
    @Bindable
    public PropertyAdapter getPropertyAdapter() {
        return mPropertyAdapter;
    }

    @NonNull
    @Bindable
    public ControlPanelModel getControlPanelModel() {
        return mControlPanelModel;
    }

    @Override
    public void onCompletion() {
        mControlPanelModel.terminate();
        if (!selectNext()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
        EventLogger.sendPlayContent(true);
    }

    @Override
    public void next() {
        mControlPanelModel.terminate();
        if (!selectNext()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
        EventLogger.sendPlayContent(true);
    }

    @Override
    public void previous() {
        mControlPanelModel.terminate();
        if (!selectPrevious()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
        EventLogger.sendPlayContent(true);
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
