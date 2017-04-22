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
import android.widget.Toast;

import com.android.databinding.library.baseAdapters.BR;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.util.DownloadUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PhotoActivityModel extends BaseObservable {
    @NonNull
    public final String title;
    @Nullable
    private byte[] mImageBinary;
    private boolean mLoading = true;
    private int mRightNavigationSize;

    @NonNull
    private final Activity mActivity;
    @NonNull
    private final PlaybackTargetModel mTargetModel;

    public PhotoActivityModel(@NonNull final Activity activity,
                              @NonNull final Repository repository) {
        mTargetModel = repository.getPlaybackTargetModel();
        if (mTargetModel == null) {
            throw new IllegalStateException();
        }
        mActivity = activity;
        title = AribUtils.toDisplayableString(mTargetModel.getCdsObject().getTitle());
        final String url = mTargetModel.getUri().toString();
        DownloadUtils.async(url, data -> {
            if (data == null) {
                showToast(R.string.toast_download_error_occurred);
                return;
            }
            setLoading(false);
            setImageBinary(data);
        });
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

    public void onClickBack() {
        mActivity.onBackPressed();
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

    private void showToast(int resId) {
        Toast.makeText(mActivity, resId, Toast.LENGTH_LONG).show();
    }
}
