/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.util.ViewUtils;
import net.mm2d.android.view.TransitionListenerAdapter;
import net.mm2d.dmsexplorer.databinding.ServerListActivityBinding;
import net.mm2d.dmsexplorer.domain.model.ControlPointModel;
import net.mm2d.dmsexplorer.model.ServerListActivityModel;
import net.mm2d.dmsexplorer.model.ServerListActivityModel.ServerSelectListener;

import java.util.List;
import java.util.Map;

/**
 * MediaServerのサーチ、選択を行うActivity。
 *
 * <p>アプリ起動時最初に表示されるActivity
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerListActivity extends AppCompatActivity
        implements ServerSelectListener {
    private static final String KEY_SCROLL_POSITION = "KEY_SCROLL_POSITION";
    private static final String KEY_SCROLL_OFFSET = "KEY_SCROLL_OFFSET";
    private static final String KEY_HAS_REENTER_TRANSITION = "KEY_HAS_REENTER_TRANSITION";
    private boolean mHasReenterTransition;
    private boolean mTwoPane;
    private final ControlPointModel mControlPointModel = DataHolder.getInstance().getControlPointModel();
    private ServerDetailFragment mServerDetailFragment;
    private ServerListActivityBinding mBinding;

    @Override
    public void onSelect(@NonNull final View v, @NonNull final MediaServer server, boolean alreadySelected) {
        if (mTwoPane) {
            if (alreadySelected) {
                startCdsListActivity(v, server);
                return;
            }
            setDetailFragment(server, true);
        } else {
            startServerDetailActivity(v, server);
        }
    }

    @Override
    public void onUnselect() {
        removeDetailFragment();
    }

    @Override
    public void onDetermine(@NonNull final View v, @NonNull final MediaServer server) {
        startCdsListActivity(v, server);
    }

    private void startServerDetailActivity(final @NonNull View v, final @NonNull MediaServer server) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServerDetailActivityLollipop(v, server);
        } else {
            startServerDetailActivityJellyBean(v, server);
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void startServerDetailActivityLollipop(final @NonNull View v, final @NonNull MediaServer server) {
        final Intent intent = ServerDetailActivity.makeIntent(this, server.getUdn());
        intent.putExtra(Const.EXTRA_HAS_TRANSITION, true);
        final View accent = v.findViewById(R.id.accent);
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                sharedElements.clear();
                sharedElements.put(Const.SHARE_ELEMENT_NAME_DEVICE_ICON, accent);
            }
        });
        startActivity(intent, ActivityOptions
                .makeSceneTransitionAnimation(ServerListActivity.this,
                        new Pair<>(accent, Const.SHARE_ELEMENT_NAME_DEVICE_ICON))
                .toBundle());
        mHasReenterTransition = true;
    }

    private void startServerDetailActivityJellyBean(final @NonNull View v, final @NonNull MediaServer server) {
        final Intent intent = ServerDetailActivity.makeIntent(this, server.getUdn());
        startActivity(intent,
                ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
    }

    private void startCdsListActivity(final @NonNull View v, final @NonNull MediaServer server) {
        final Intent intent = CdsListActivity.makeIntent(this, server.getUdn());
        startActivity(intent, ActivityOptions.makeScaleUpAnimation(
                v, 0, 0, v.getWidth(), v.getHeight())
                .toBundle());
    }

    private void setDetailFragment(final @NonNull MediaServer server, boolean animate) {
        mServerDetailFragment = ServerDetailFragment.newInstance(server.getUdn());
        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mServerDetailFragment.setEnterTransition(new Slide(Gravity.START));
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.serverDetailContainer, mServerDetailFragment)
                .commit();
    }

    private void removeDetailFragment() {
        if (!mTwoPane || mServerDetailFragment == null) {
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .remove(mServerDetailFragment)
                .commit();
        mServerDetailFragment = null;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.defaultStatusBar));
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.server_list_activity);
        mBinding.setModel(new ServerListActivityModel(this, this));
        mTwoPane = mBinding.serverDetailContainer != null;

        setSupportActionBar(mBinding.toolbar);

        if (savedInstanceState == null) {
            mControlPointModel.initialize();
        } else {
            mHasReenterTransition = savedInstanceState.getBoolean(KEY_HAS_REENTER_TRANSITION);
            final int position = savedInstanceState.getInt(KEY_SCROLL_POSITION);
            final int offset = savedInstanceState.getInt(KEY_SCROLL_OFFSET);
            final RecyclerView recyclerView = mBinding.serverList;
            ViewUtils.execOnLayout(recyclerView, () -> {
                recyclerView.scrollToPosition(position);
                recyclerView.post(() -> recyclerView.scrollBy(0, offset));
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            mControlPointModel.terminate();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        removeDetailFragment();
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_HAS_REENTER_TRANSITION, mHasReenterTransition);
        final RecyclerView recyclerView = mBinding.serverList;
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
        mHasReenterTransition = false;
    }

    private void updateState() {
        final int position = mControlPointModel.findSelectedMediaServerPosition();
        if (mTwoPane) {
            mBinding.getModel().updateListAdapter();
            if (position < 0) {
                removeDetailFragment();
                return;
            }
            setDetailFragment(mControlPointModel.getSelectedMediaServer(), false);
            return;
        }
        if (position < 0) {
            mBinding.getModel().updateListAdapter();
            clearExitSharedElement();
            return;
        }
        if (mHasReenterTransition && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            prepareReenterTransition();
            return;
        }
        mBinding.getModel().updateListAdapter();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void prepareReenterTransition() {
        resetExitSharedElement();
        getWindow().getSharedElementExitTransition().addListener(new TransitionListenerAdapter() {
            @TargetApi(VERSION_CODES.KITKAT)
            @Override
            public void onTransitionEnd(Transition transition) {
                mBinding.getModel().updateListAdapter();
                transition.removeListener(this);
            }
        });
    }

    private void resetExitSharedElement() {
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                sharedElements.clear();
                final View shared = mBinding.getModel().findSharedView();
                if (shared != null) {
                    sharedElements.put(Const.SHARE_ELEMENT_NAME_DEVICE_ICON, shared);
                }
            }
        });
    }

    private void clearExitSharedElement() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    sharedElements.clear();
                }
            });
        }
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
