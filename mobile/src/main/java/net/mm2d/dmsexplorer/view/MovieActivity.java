/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import net.mm2d.dmsexplorer.BuildConfig;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.MovieActivityBinding;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.util.RepeatIntroductionUtils;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.viewmodel.ControlPanelModel;
import net.mm2d.dmsexplorer.viewmodel.MovieActivityModel;
import net.mm2d.dmsexplorer.viewmodel.MovieActivityModel.OnChangeContentListener;
import net.mm2d.util.TextUtils;

import java.util.concurrent.TimeUnit;

/**
 * 動画再生のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MovieActivity extends BaseActivity implements OnChangeContentListener {
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final long TIMEOUT_DELAY = TimeUnit.SECONDS.toMillis(1);
    private static final String PREFIX = BuildConfig.APPLICATION_ID + ".";
    private static final String ACTION_PLAY = PREFIX + "ACTION_PLAY";
    private static final String ACTION_NEXT = PREFIX + "ACTION_NEXT";
    private static final String ACTION_PREV = PREFIX + "ACTION_PREV";
    private FullscreenHelper mFullscreenHelper;
    private MovieActivityModel mModel;
    private BroadcastReceiver mControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(
                Context context,
                Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.isEmpty(action) || mModel == null) {
                return;
            }
            final ControlPanelModel control = mModel.getControlPanelModel();
            switch (intent.getAction()) {
                case ACTION_PLAY:
                    control.onClickPlay();
                    break;
                case ACTION_NEXT:
                    control.onClickNext();
                    break;
                case ACTION_PREV:
                    control.onClickPrevious();
                    break;
            }
        }
    };

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
        mModel.setOnChangeContentListener(this);
        binding.setModel(mModel);
        mModel.adjustPanel(this);
        if (RepeatIntroductionUtils.show(this, binding.repeatButton)) {
            final long timeout = RepeatIntroductionUtils.TIMEOUT + TIMEOUT_DELAY;
            mFullscreenHelper.showNavigation(timeout);
        } else {
            mFullscreenHelper.showNavigation();
        }
        if (savedInstanceState != null) {
            final int progress = savedInstanceState.getInt(KEY_POSITION, 0);
            mModel.restoreSaveProgress(progress);
        }
        registerReceiver(mControlReceiver, makeIntentFilter());
    }

    @NonNull
    private IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREV);
        return intentFilter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModel != null) {
            mModel.terminate();
        }
        mFullscreenHelper.terminate();
        unregisterReceiver(mControlReceiver);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        mFullscreenHelper.onPictureInPictureModeChanged(isInPictureInPictureMode);
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

    @NonNull
    public static PendingIntent makePlayPendingIntent(@NonNull final Context context) {
        return PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_ACTION_PLAY,
                new Intent(ACTION_PLAY),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static PendingIntent makeNextPendingIntent(@NonNull final Context context) {
        return PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_ACTION_NEXT,
                new Intent(ACTION_NEXT),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static PendingIntent makePreviousPendingIntent(@NonNull final Context context) {
        return PendingIntent.getBroadcast(context,
                Const.REQUEST_CODE_ACTION_PREVIOUS,
                new Intent(ACTION_PREV),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
