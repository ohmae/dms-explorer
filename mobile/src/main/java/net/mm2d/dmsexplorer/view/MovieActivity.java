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
    private MovieActivityModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MovieActivityBinding binding
                = DataBindingUtil.setContentView(this, R.layout.movie_activity);
        mFullscreenHelper = new FullscreenHelper.Builder(binding.getRoot())
                .setTopView(binding.toolbar)
                .setBottomView(binding.controlPanel.getRoot())
                .build();
        final Repository repository = Repository.get();
        try {
            mModel = new MovieActivityModel(this, binding.videoView, repository);
        } catch (final IllegalStateException ignored) {
            finish();
            return;
        }
        binding.setModel(mModel);
        mModel.adjustPanel(this);
        mFullscreenHelper.showNavigation();
        if (savedInstanceState != null) {
            final int progress = savedInstanceState.getInt(KEY_POSITION, 0);
            mModel.restoreSaveProgress(progress);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModel != null) {
            mModel.terminate();
        }
        mFullscreenHelper.terminate();
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
        if (mModel != null) {
            outState.putInt(KEY_POSITION, mModel.getCurrentProgress());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mModel != null) {
            mModel.adjustPanel(this);
        }
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
