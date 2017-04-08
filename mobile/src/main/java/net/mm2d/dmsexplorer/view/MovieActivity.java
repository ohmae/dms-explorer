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
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.MovieActivityBinding;
import net.mm2d.dmsexplorer.domain.model.PlaybackTargetModel;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.viewmodel.MovieActivityModel;

/**
 * 動画再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MovieActivity extends AppCompatActivity {
    private FullscreenHelper mFullscreenHelper;
    private MovieActivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.movie_activity);
        final Repository repository = Repository.get();
        final MovieActivityModel model = MovieActivityModel.create(this, repository);
        if (model == null) {
            finish();
            return;
        }
        mBinding.setModel(model);
        mFullscreenHelper = new FullscreenHelper.Builder(mBinding.getRoot())
                .setTopView(mBinding.toolbar)
                .setBottomView(mBinding.controlPanel)
                .build();
        mFullscreenHelper.showNavigation();

        mBinding.toolbarBack.setOnClickListener(view -> onBackPressed());
        mBinding.controlPanel.setOnCompletionListener(mp -> onBackPressed());
        mBinding.controlPanel.setOnUserActionListener(mFullscreenHelper::postHideNavigation);
        mBinding.videoView.setOnPreparedListener(mBinding.controlPanel);
        final PlaybackTargetModel targetModel = repository.getPlaybackTargetModel();
        mBinding.videoView.setVideoURI(targetModel.getUri());
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
        mBinding.videoView.stopPlayback();
        mFullscreenHelper.onDestroy();
    }
}
