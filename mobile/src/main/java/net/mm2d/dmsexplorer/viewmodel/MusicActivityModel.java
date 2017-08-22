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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.domain.model.MusicPlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.settings.RepeatMode;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.DownloadUtils;
import net.mm2d.dmsexplorer.util.ThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.ContentPropertyAdapter;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.OnCompletionListener;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.SkipControlListener;

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
    private ContentPropertyAdapter mPropertyAdapter;
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
        updateTargetModel();
    }

    private void updateTargetModel() {
        final PlayerModel playerModel = new MusicPlayerModel(mActivity);
        mControlPanelModel = new ControlPanelModel(mActivity, playerModel);
        final PlaybackTargetModel targetModel = mRepository.getPlaybackTargetModel();
        if (targetModel == null || targetModel.getUri() == null) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        mControlPanelModel.setRepeatMode(mRepeatMode);
        mControlPanelModel.setOnCompletionListener(this);
        mControlPanelModel.setSkipControlListener(this);
        playerModel.setUri(targetModel.getUri(), null);

        mTitle = AribUtils.toDisplayableString(targetModel.getTitle());
        mAccentColor = ThemeUtils.getDeepColor(mTitle);
        mPropertyAdapter = new ContentPropertyAdapter(mActivity, targetModel.getCdsObject());
        mRepository.getThemeModel().setThemeColor(mActivity, mAccentColor, 0);

        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.accentColor);
        notifyPropertyChanged(BR.propertyAdapter);
        notifyPropertyChanged(BR.controlPanelModel);

        controlPanelParam.setBackgroundColor(mAccentColor);

        loadArt(targetModel.getCdsObject().getValue(CdsObject.UPNP_ALBUM_ART_URI));
    }

    private void loadArt(@Nullable final String url) {
        setImageBinary(null);
        if (url != null) {
            DownloadUtils.async(url, this::setImageBinary);
        }
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
    public ContentPropertyAdapter getPropertyAdapter() {
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
    }

    @Override
    public void next() {
        mControlPanelModel.terminate();
        if (!selectNext()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
    }

    @Override
    public void previous() {
        mControlPanelModel.terminate();
        if (!selectPrevious()) {
            ActivityCompat.finishAfterTransition(mActivity);
            return;
        }
        updateTargetModel();
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
