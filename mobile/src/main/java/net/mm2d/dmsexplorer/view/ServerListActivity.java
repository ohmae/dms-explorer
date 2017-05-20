/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.mm2d.android.util.ActivityUtils;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.dmsexplorer.Const;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.Repository;
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.viewmodel.ServerListActivityModel;
import net.mm2d.dmsexplorer.viewmodel.ServerListActivityModel.ServerSelectListener;

import java.util.List;
import java.util.Map;

/**
 * MediaServerのサーチ、選択を行うActivity。
 *
 * <p>アプリ起動時最初に表示されるActivity
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerListActivity extends BaseActivity
        implements ServerSelectListener {
    private static final String KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION";
    private static final String KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET";
    private boolean mTwoPane;
    private final ControlPointModel mControlPointModel
            = Repository.get().getControlPointModel();
    private Fragment mFragment;
    private ServerListActivityBinding mBinding;

    @Override
    public void onSelect(@NonNull final View v, boolean alreadySelected) {
        if (mTwoPane) {
            if (alreadySelected) {
                startCdsListActivity(v);
                return;
            }
            setDetailFragment(true);
        } else {
            startServerDetailActivity(v);
        }
    }

    @Override
    public void onUnselect() {
        removeDetailFragment();
    }

    @Override
    public void onDetermine(@NonNull final View v) {
        startCdsListActivity(v);
    }

    private void startServerDetailActivity(@NonNull final View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServerDetailActivityLollipop(v);
        } else {
            startServerDetailActivityJellyBean(v);
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void startServerDetailActivityLollipop(@NonNull final View v) {
        final Intent intent = ServerDetailActivity.makeIntent(this);
        final View accent = v.findViewById(R.id.accent);
        startActivity(intent, ActivityOptions
                .makeSceneTransitionAnimation(ServerListActivity.this,
                        new Pair<>(accent, Const.SHARE_ELEMENT_NAME_DEVICE_ICON))
                .toBundle());
    }

    private void startServerDetailActivityJellyBean(@NonNull final View v) {
        final Intent intent = ServerDetailActivity.makeIntent(this);
        startActivity(intent, ActivityUtils.makeScaleUpAnimationBundle(v));
    }

    private void startCdsListActivity(@NonNull final View v) {
        final Intent intent = ContentListActivity.makeIntent(this);
        startActivity(intent, ActivityOptions.makeScaleUpAnimation(
                v, 0, 0, v.getWidth(), v.getHeight())
                .toBundle());
    }

    private void setDetailFragment(boolean animate) {
        mFragment = ServerDetailFragment.newInstance();
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("RtlHardcoded")
            final int gravity = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                    ? Gravity.START : Gravity.LEFT;
            mFragment.setEnterTransition(new Slide(gravity));
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.server_detail_container, mFragment)
                .commitAllowingStateLoss();
    }

    private void removeDetailFragment() {
        if (!mTwoPane || mFragment == null) {
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mFragment)
                .commitAllowingStateLoss();
        mFragment = null;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Repository repository = Repository.get();
        repository.getThemeModel().setThemeColor(this,
                ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.defaultStatusBar));

        mBinding = DataBindingUtil.setContentView(this, R.layout.server_list_activity);
        mBinding.setModel(new ServerListActivityModel(this, repository, this));
        mTwoPane = mBinding.serverDetailContainer != null;

        setSupportActionBar(mBinding.toolbar);

        if (savedInstanceState == null) {
            mControlPointModel.initialize();
        } else {
            restoreScroll(savedInstanceState);
        }
        setSharedElementCallback();
    }

    private void setSharedElementCallback() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(final List<String> names, final Map<String, View> sharedElements) {
                    sharedElements.clear();
                    final View shared = mBinding.getModel().findSharedView();
                    if (shared != null) {
                        sharedElements.put(Const.SHARE_ELEMENT_NAME_DEVICE_ICON, shared);
                    }
                }
            });
        }
    }

    private void restoreScroll(@NonNull final Bundle savedInstanceState) {
        final int position = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0);
        final int offset = savedInstanceState.getInt(KEY_SCROLL_OFFSET, 0);
        if (position == 0 && offset == 0) {
            return;
        }
        final RecyclerView recyclerView = mBinding.recyclerView;
        ViewUtils.execOnLayout(recyclerView, () -> {
            recyclerView.scrollToPosition(position);
            recyclerView.post(() -> recyclerView.scrollBy(0, offset));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            mControlPointModel.terminate();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        removeDetailFragment();
        super.onSaveInstanceState(outState);
        saveScroll(outState);
    }

    private void saveScroll(@NonNull final Bundle outState) {
        final RecyclerView recyclerView = mBinding.recyclerView;
        if (recyclerView.getChildCount() == 0) {
            return;
        }
        final View view = recyclerView.getChildAt(0);
        outState.putInt(KEY_SCROLL_POSITION, recyclerView.getChildAdapterPosition(view));
        outState.putInt(KEY_SCROLL_OFFSET, -view.getTop());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mControlPointModel.searchStart();
        updateState();
    }

    private void updateState() {
        mBinding.getModel().updateListAdapter();
        if (mTwoPane) {
            updateFragmentState();
        }
    }

    private void updateFragmentState() {
        if (mBinding.getModel().hasSelectedMediaServer()) {
            setDetailFragment(false);
            return;
        }
        removeDetailFragment();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mControlPointModel.searchStop();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(SettingsActivity.makeIntent(this));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
