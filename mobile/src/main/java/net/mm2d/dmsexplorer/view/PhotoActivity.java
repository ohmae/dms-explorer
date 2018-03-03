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
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import net.mm2d.dmsexplorer.EventLogger;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.PhotoActivityBinding;
import net.mm2d.dmsexplorer.domain.model.MediaServerModel;
import net.mm2d.dmsexplorer.util.FullscreenHelper;
import net.mm2d.dmsexplorer.view.base.BaseActivity;
import net.mm2d.dmsexplorer.view.view.ViewPagerAdapter;
import net.mm2d.dmsexplorer.viewmodel.PhotoActivityModel;

/**
 * 静止画表示のActivity。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PhotoActivity extends BaseActivity {
    private FullscreenHelper mFullscreenHelper;
    private PhotoActivityBinding mBinding;
    private PhotoActivityModel mModel;
    private Repository mRepository;
    private MediaServerModel mServerModel;
    private final OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(
                final int position,
                final float positionOffset,
                final int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(final int position) {
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                onScrollIdle();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.photo_activity);
        mFullscreenHelper = new FullscreenHelper.Builder(mBinding.getRoot())
                .setTopView(mBinding.toolbar)
                .build();
        mRepository = Repository.get();
        mServerModel = mRepository.getMediaServerModel();
        try {
            mModel = new PhotoActivityModel(this, mRepository);
        } catch (final IllegalStateException ignored) {
            finish();
            return;
        }

        mBinding.setModel(mModel);
        mModel.adjustPanel(this);
        mFullscreenHelper.showNavigation();

        final ViewPagerAdapter pagerAdapter = new ViewPagerAdapter();
        final LayoutInflater inflater = LayoutInflater.from(this);
        pagerAdapter.add(inflater.inflate(R.layout.progress_view, mBinding.viewPager, false));
        pagerAdapter.add(inflater.inflate(R.layout.transparent_view, mBinding.viewPager, false));
        pagerAdapter.add(inflater.inflate(R.layout.progress_view, mBinding.viewPager, false));
        mBinding.viewPager.setAdapter(pagerAdapter);
        mBinding.viewPager.setCurrentItem(1, false);
        mBinding.viewPager.addOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFullscreenHelper.terminate();
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP
                && event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
            mFullscreenHelper.showNavigation();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mModel != null) {
            mModel.adjustPanel(this);
        }
    }

    private void onScrollIdle() {
        final int index = mBinding.viewPager.getCurrentItem();
        if (index == 1) {
            return;
        }
        if (!move(index)) {
            finish();
            return;
        }
        mModel = new PhotoActivityModel(this, mRepository);
        mBinding.setModel(mModel);
        mBinding.viewPager.setCurrentItem(1, false);
        EventLogger.sendPlayContent(true);
    }

    private boolean move(int index) {
        return index == 0 ? mServerModel.selectPreviousEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL)
                : mServerModel.selectNextEntity(MediaServerModel.SCAN_MODE_SEQUENTIAL);
    }
}
