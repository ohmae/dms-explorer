/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.PhotoActivityBinding;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.util.ImageViewUtils;
import net.mm2d.dmsexplorer.viewmodel.PhotoActivityModel;

/**
 * 静止画表示のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class PhotoActivity extends AppCompatActivity {
    private FullscreenHelper mFullscreenHelper;
    private PhotoActivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.photo_activity);
        final PhotoActivityModel model = PhotoActivityModel.create(this, Repository.get());
        if (model == null) {
            finish();
            return;
        }
        mBinding.setModel(model);
        mBinding.toolbarBack.setOnClickListener(view -> onBackPressed());

        mFullscreenHelper = new FullscreenHelper.Builder(mBinding.getRoot())
                .setTopView(mBinding.toolbar)
                .build();
        mFullscreenHelper.showNavigation();

        final PlaybackTargetModel targetModel = Repository.get().getPlaybackTargetModel();
        downloadAndSetImage(targetModel.getUri().toString());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mBinding.getModel().adjustPanel(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFullscreenHelper.onDestroy();
    }

    private void downloadAndSetImage(@NonNull final String url) {
        ImageViewUtils.downloadAndSetImage(mBinding.imageView, url, new ImageViewUtils.Callback() {
            @Override
            public void onSuccess() {
                mBinding.getModel().setLoading(false);
            }

            @Override
            public void onError() {
                showToast(R.string.toast_download_error_occurred);
            }
        });
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }
}
