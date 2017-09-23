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
import android.view.KeyEvent;
import android.view.MotionEvent;

import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.MovieActivityBinding;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.util.RepeatIntroductionUtils;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel;
import net.mm2d.dmsexplorer.viewmodel.MovieActivityModel;
import net.mm2d.dmsexplorer.viewmodel.MovieActivityModel.OnChangeContentListener;

import java.util.concurrent.TimeUnit;

/**
 * 動画再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MovieActivity extends BaseActivity implements OnChangeContentListener {
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final long TIMEOUT_DELAY = TimeUnit.SECONDS.toMillis(1);
    private FullscreenHelper mFullscreenHelper;
    private MovieActivityBinding mBinding;
    private MovieActivityModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.movie_activity);
        mFullscreenHelper = new FullscreenHelper.Builder(mBinding.getRoot())
                .setTopView(mBinding.toolbar)
                .setBottomView(mBinding.controlPanel.getRoot())
                .build();
        final Repository repository = Repository.get();
        try {
            mModel = new MovieActivityModel(this, mBinding.videoView, repository);
        } catch (final IllegalStateException ignored) {
            finish();
            return;
        }
        mModel.setOnChangeContentListener(this);
        mBinding.setModel(mModel);
        mModel.adjustPanel(this);
        if (RepeatIntroductionUtils.show(this, mBinding.repeatButton)) {
            final long timeout = RepeatIntroductionUtils.TIMEOUT + TIMEOUT_DELAY;
            mFullscreenHelper.showNavigation(timeout);
        } else {
            mFullscreenHelper.showNavigation();
        }
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
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        mFullscreenHelper.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP
                && event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
            if (mFullscreenHelper.showNavigation()) {
                mBinding.controlPanel.playPause.requestFocus();
            }
            final ControlPanelModel control = mModel.getControlPanelModel();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    control.onClickPlayPause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    control.onClickPlay();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    control.onClickPause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                    control.onClickNext();
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                    control.onClickPrevious();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
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
    public void onChangeContent() {
        mFullscreenHelper.showNavigation();
    }
}
