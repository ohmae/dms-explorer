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
import android.support.annotation.Nullable;
import android.widget.VideoView;

import com.android.databinding.library.baseAdapters.BR;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.MediaPlayerModel;
import net.mm2d.dmsexplorer.domain.model.MoviePlayerModel;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.viewmodel.ControlViewModel.OnCompletionListener;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MovieActivityModel extends BaseObservable implements OnCompletionListener {
    private String mTitle;
    private ControlViewModel mControlViewModel;
    private int mRightNavigationSize;
    private int mBottomNavigationSize;

    private final Activity mActivity;

    @Nullable
    public static MovieActivityModel create(@NonNull final Activity activity,
                                            @NonNull final VideoView videoView,
                                            @NonNull final Repository repository) {
        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        if (targetModel == null) {
            return null;
        }
        return new MovieActivityModel(activity, videoView, repository);
    }

    private MovieActivityModel(@NonNull final Activity activity,
                               @NonNull final VideoView videoView,
                               @NonNull final Repository repository) {
        mActivity = activity;

        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        final MediaPlayerModel playerModel = new MoviePlayerModel(videoView);
        mControlViewModel = new ControlViewModel(playerModel);
        mControlViewModel.setOnCompletionListener(this);
        playerModel.setUri(targetModel.getUri());
        mTitle = AribUtils.toDisplayableString(targetModel.getTitle());
    }

    public void adjustPanel(@NonNull final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                || (VERSION.SDK_INT >= VERSION_CODES.N && activity.isInMultiWindowMode())) {
            setRightNavigationSize(0);
            setBottomNavigationSize(0);
            return;
        }
        final Point p1 = DisplaySizeUtils.getSize(activity);
        final Point p2 = DisplaySizeUtils.getRealSize(activity);
        setRightNavigationSize(p2.x - p1.x);
        setBottomNavigationSize(p2.y - p1.y);
    }

    public void terminate() {
        mControlViewModel.terminate();
    }

    public void restoreSaveProgress(final int position) {
        mControlViewModel.restoreSaveProgress(position);
    }

    public int getCurrentProgress() {
        return mControlViewModel.getProgress();
    }

    public void onClickBack() {
        mActivity.onBackPressed();
    }

    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Bindable
    public ControlViewModel getControlViewModel() {
        return mControlViewModel;
    }

    @Bindable
    public int getRightNavigationSize() {
        return mRightNavigationSize;
    }

    private void setRightNavigationSize(final int rightNavigationSize) {
        mRightNavigationSize = rightNavigationSize;
        notifyPropertyChanged(BR.rightNavigationSize);
    }

    @Bindable
    public int getBottomNavigationSize() {
        return mBottomNavigationSize;
    }

    private void setBottomNavigationSize(final int bottomNavigationSize) {
        mBottomNavigationSize = bottomNavigationSize;
        notifyPropertyChanged(BR.bottomNavigationSize);
    }

    @Override
    public void onCompletion() {
        mActivity.onBackPressed();
    }
}
