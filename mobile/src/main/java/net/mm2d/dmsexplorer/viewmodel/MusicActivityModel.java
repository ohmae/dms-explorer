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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.util.AribUtils;
import net.mm2d.dmsexplorer.BR;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MusicPlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.util.DownloadUtils;
import net.mm2d.dmsexplorer.util.ThemeUtils;
import net.mm2d.dmsexplorer.view.adapter.ContentPropertyAdapter;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.OnCompletionListener;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MusicActivityModel extends BaseObservable implements OnCompletionListener {
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
    private final Activity mActivity;
    @NonNull
    private final Repository mRepository;

    public MusicActivityModel(@NonNull final Activity activity,
                              @NonNull final Repository repository) {
        mActivity = activity;
        mRepository = repository;

        controlPanelParam = new ControlPanelParam();
        updateTargetModel();
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

    private void updateTargetModel() {
        final PlaybackTargetModel targetModel = mRepository.getPlaybackTargetModel();
        final PlayerModel playerModel = new MusicPlayerModel(mActivity);
        mControlPanelModel = new ControlPanelModel(mActivity, playerModel);
        mControlPanelModel.setOnCompletionListener(this);
        playerModel.setUri(targetModel.getUri(), null);

        mTitle = AribUtils.toDisplayableString(targetModel.getTitle());
        mAccentColor = ThemeUtils.getDeepColor(mTitle);
        mPropertyAdapter = new ContentPropertyAdapter(mActivity, targetModel.getCdsObject());
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mActivity.getWindow().setStatusBarColor(ThemeUtils.getDarkerColor(mAccentColor));
        }
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

    @Bindable
    public byte[] getImageBinary() {
        return mImageBinary;
    }

    public void setImageBinary(final byte[] imageBinary) {
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

    @Bindable
    public ContentPropertyAdapter getPropertyAdapter() {
        return mPropertyAdapter;
    }

    @Bindable
    public ControlPanelModel getControlPanelModel() {
        return mControlPanelModel;
    }

    @Override
    public void onCompletion() {
        mControlPanelModel.terminate();
        if (!mRepository.getMediaServerModel().selectNextObject()) {
            mActivity.onBackPressed();
            return;
        }
        updateTargetModel();
    }
}
