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
import android.view.MotionEvent;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.MovieActivityBinding;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.viewmodel.MovieActivityModel;

/**
 * 動画再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MovieActivity extends AppCompatActivity {
    private static final String KEY_POSITION = "KEY_POSITION";
    private FullscreenHelper mFullscreenHelper;
    private MovieActivityBinding mBinding;
    private MovieActivityModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.movie_activity);
        final Repository repository = Repository.get();
        mModel = MovieActivityModel.create(this, mBinding.videoView, repository);
        if (mModel == null) {
            finish();
            return;
        }
        mBinding.setModel(mModel);
        mModel.adjustPanel(this);

        mFullscreenHelper = new FullscreenHelper.Builder(mBinding.getRoot())
                .setTopView(mBinding.toolbar)
                .setBottomView(mBinding.controlPanel)
                .build();
        mFullscreenHelper.showNavigation();

        if (savedInstanceState != null) {
            final int progress = savedInstanceState.getInt(KEY_POSITION, 0);
            mModel.restoreSaveProgress(progress);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mModel.terminate();
        mFullscreenHelper.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        final boolean result = super.dispatchTouchEvent(ev);
        mFullscreenHelper.showNavigation();
        return result;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mModel.getCurrentProgress());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mModel.adjustPanel(this);
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
}
