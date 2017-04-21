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

import com.android.databinding.library.baseAdapters.BR;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PhotoActivityModel extends BaseObservable {
    public final String title;

    private boolean mLoading = true;
    private int mRightNavigationSize;

    public static PhotoActivityModel create(@NonNull final Activity activity,
                                            @NonNull final Repository repository) {
        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        if (targetModel == null) {
            return null;
        }
        return new PhotoActivityModel(activity, targetModel);
    }

    private PhotoActivityModel(@NonNull final Activity activity,
                               @NonNull final PlaybackTargetModel targetModel) {
        title = AribUtils.toDisplayableString(targetModel.getCdsObject().getTitle());
        adjustPanel(activity);
    }

    public void adjustPanel(@NonNull final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                || (VERSION.SDK_INT >= VERSION_CODES.N && activity.isInMultiWindowMode())) {
            setRightNavigationSize(0);
            return;
        }
        final Point p1 = DisplaySizeUtils.getSize(activity);
        final Point p2 = DisplaySizeUtils.getRealSize(activity);
        setRightNavigationSize(p2.x - p1.x);
    }

    @Bindable
    public int getRightNavigationSize() {
        return mRightNavigationSize;
    }

    public void setRightNavigationSize(final int rightNavigationSize) {
        mRightNavigationSize = rightNavigationSize;
        notifyPropertyChanged(BR.rightNavigationSize);
    }

    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(final boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
    }
}
