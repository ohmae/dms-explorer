/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.MusicActivityBinding;
import net.mm2d.dmsexplorer.util.RepeatIntroductionUtils;
import net.mm2d.dmsexplorer.viewmodel.MusicActivityModel;

/**
 * 音楽再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MusicActivity extends BaseActivity {
    private static final String KEY_POSITION = "KEY_POSITION";
    private MusicActivityModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Repository repository = Repository.get();
        final MusicActivityBinding binding
                = DataBindingUtil.setContentView(this, R.layout.music_activity);
        try {
            mModel = new MusicActivityModel(this, repository);
            binding.setModel(mModel);
        } catch (final IllegalStateException ignored) {
            return;
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            final int progress = savedInstanceState.getInt(KEY_POSITION, 0);
            mModel.restoreSaveProgress(progress);
        }
        RepeatIntroductionUtils.show(this, binding.repeatButton);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mModel != null) {
            outState.putInt(KEY_POSITION, mModel.getCurrentProgress());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModel != null) {
            mModel.terminate();
        }
    }
}
