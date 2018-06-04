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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.android.databinding.library.baseAdapters.BR;

import net.mm2d.android.util.AribUtils;
import net.mm2d.android.util.DisplaySizeUtils;
import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.Downloader;
import net.mm2d.dmsexplorer.view.base.BaseActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PhotoActivityModel extends BaseObservable {
    @NonNull
    public final String title;
    @ColorInt
    public final int background;
    @Nullable
    private byte[] mImageBinary;
    private boolean mLoading = true;
    private int mRightNavigationSize;

    @NonNull
    private final BaseActivity mActivity;
    @NonNull
    private final PlaybackTargetModel mTargetModel;

    public PhotoActivityModel(
            @NonNull final BaseActivity activity,
            @NonNull final Repository repository) {
        mTargetModel = repository.getPlaybackTargetModel();
        if (mTargetModel == null) {
            throw new IllegalStateException();
        }
        mActivity = activity;
        final Settings settings = Settings.get();
        title = settings.shouldShowTitleInPhotoUi()
                ? AribUtils.toDisplayableString(mTargetModel.getTitle())
                : "";
        background = settings.isPhotoUiBackgroundTransparent()
                ? Color.TRANSPARENT
                : ContextCompat.getColor(activity, R.color.translucent_control);
        final Uri uri = mTargetModel.getUri();
        if (uri == Uri.EMPTY) {
            throw new IllegalStateException();
        }
        Downloader.create(uri.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    final PlaybackTargetModel model = repository.getPlaybackTargetModel();
                    if (model == null) {
                        return;
                    }
                    if (!uri.equals(model.getUri())) {
                        return;
                    }
                    setLoading(false);
                    setImageBinary(data);
                }, throwable -> Toaster.show(mActivity, R.string.toast_download_error));
    }

    public void adjustPanel(@NonNull final Activity activity) {
        final Point size = DisplaySizeUtils.getNavigationBarArea(activity);
        setRightNavigationSize(size.x);
    }

    public void onClickBack() {
        mActivity.navigateUpTo();
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
}
