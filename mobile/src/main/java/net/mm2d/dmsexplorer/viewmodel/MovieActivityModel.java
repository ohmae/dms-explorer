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
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.VideoView;

import com.android.databinding.library.baseAdapters.BR;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MoviePlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.domain.model.PlayerModel;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel.OnCompletionListener;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MovieActivityModel extends BaseObservable implements OnCompletionListener {
    @NonNull
    public final ControlPanelParam controlPanelParam;

    @NonNull
    private String mTitle;
    @NonNull
    private ControlPanelModel mControlPanelModel;
    private int mRightNavigationSize;
    @NonNull
    private final Activity mActivity;

    public MovieActivityModel(@NonNull final Activity activity,
                              @NonNull final VideoView videoView,
                              @NonNull final Repository repository) {
        mActivity = activity;

        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        if (targetModel == null) {
            throw new IllegalStateException();
        }
        final PlayerModel playerModel = new MoviePlayerModel(videoView);
        mControlPanelModel = new ControlPanelModel(activity, playerModel);
        mControlPanelModel.setOnCompletionListener(this);
        playerModel.setUri(targetModel.getUri(), null);
        mTitle = AribUtils.toDisplayableString(targetModel.getTitle());
        final int color = ContextCompat.getColor(activity, R.color.translucent_control);
        controlPanelParam = new ControlPanelParam();
        controlPanelParam.setBackgroundColor(color);
    }

    public void adjustPanel(@NonNull final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                || (VERSION.SDK_INT >= VERSION_CODES.N && activity.isInMultiWindowMode())) {
            setRightNavigationSize(0);
            controlPanelParam.setBottomPadding(0);
            return;
        }
        final Point p1 = DisplaySizeUtils.getSize(activity);
        final Point p2 = DisplaySizeUtils.getRealSize(activity);
        setRightNavigationSize(p2.x - p1.x);
        controlPanelParam.setBottomPadding(p2.y - p1.y);
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

    public void onClickBack() {
        mActivity.onBackPressed();
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
        mActivity.onBackPressed();
    }
}
