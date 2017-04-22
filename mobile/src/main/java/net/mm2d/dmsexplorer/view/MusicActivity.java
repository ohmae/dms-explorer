/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.MusicActivityBinding;
import net.mm2d.dmsexplorer.viewmodel.MusicActivityModel;

/**
 * 音楽再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MusicActivity extends AppCompatActivity {
    private static final String TAG = MusicActivity.class.getSimpleName();
    private static final String KEY_POSITION = "KEY_POSITION";
    private MusicActivityBinding mBinding;
    private Repository mRepository;
    private MusicActivityModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepository = Repository.get();
        mBinding = DataBindingUtil.setContentView(this, R.layout.music_activity);
        try {
            mModel = new MusicActivityModel(this, mRepository);
            mBinding.setModel(mModel);
        } catch (final IllegalStateException ignored) {
            return;
        }

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            final int progress = savedInstanceState.getInt(KEY_POSITION, 0);
            mModel.restoreSaveProgress(progress);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mModel.getCurrentProgress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mModel.terminate();
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
