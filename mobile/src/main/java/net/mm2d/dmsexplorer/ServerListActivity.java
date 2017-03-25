/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.mm2d.android.net.Lan;
import net.mm2d.android.upnp.AvControlPointManager;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.upnp.cds.MsControlPoint;
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener;
import net.mm2d.android.view.DividerItemDecoration;
import net.mm2d.android.view.TransitionListenerAdapter;
import net.mm2d.dmsexplorer.adapter.ServerListAdapter;
import net.mm2d.dmsexplorer.adapter.ServerListAdapter.OnItemClickListener;

import java.util.List;
import java.util.Map;

/**
 * MediaServerのサーチ、選択を行うActivity。
 *
 * <p>アプリ起動時最初に表示されるActivity
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerListActivity extends AppCompatActivity {
    private static final String KEY_HAS_REENTER_TRANSITION = "KEY_HAS_REENTER_TRANSITION";
    private static final String KEY_SELECTED_SERVER_UDN = "KEY_SELECTED_SERVER_UDN";
    private boolean mTwoPane;
    private boolean mNetworkAvailable;
    private Handler mHandler;
    private SearchThread mSearchThread;
    private final DataHolder mDataHolder = DataHolder.getInstance();
    private final AvControlPointManager mAvCpManager = mDataHolder.getAvControlPointManager();
    private final MsControlPoint mMsControlPoint = mDataHolder.getMsControlPoint();
    private MediaServer mSelectedServer;
    private ServerDetailFragment mServerDetailFragment;
    private ServerListAdapter mServerListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Lan mLan;
    private boolean mHasReenterTransition;

    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean available = mLan.hasAvailableInterface();
            if (mNetworkAvailable != available) {
                synchronized (mAvCpManager) {
                    if (available) {
                        mAvCpManager.initialize(mLan.getAvailableInterfaces());
                        mAvCpManager.start();
                    } else {
                        mAvCpManager.stop();
                        mAvCpManager.terminate();
                        showToast(R.string.no_available_network);
                        mServerListAdapter.clear();
                        mServerListAdapter.notifyDataSetChanged();
                    }
                }
                mNetworkAvailable = available;
            }
        }
    };
    private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(final @NonNull View v,
                                final int position, final @NonNull MediaServer server) {
            if (mTwoPane) {
                if (mSelectedServer != null && mSelectedServer.equals(server)) {
                    return;
                }
                mServerDetailFragment = ServerDetailFragment.newInstance(server.getUdn());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mServerDetailFragment.setEnterTransition(new Slide(Gravity.START));
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.serverDetailContainer, mServerDetailFragment)
                        .commit();
            } else {
                final Context context = v.getContext();
                final Intent intent = ServerDetailActivity.makeIntent(context, server.getUdn());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                } else {
                    startActivity(intent,
                            ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
                }
            }
            mServerListAdapter.setSelection(position);
            if (mSelectedServer != null) {
                mSelectedServer.unsubscribe();
            }
            mSelectedServer = server;
            mSelectedServer.subscribe();
        }
    };

    private final MsDiscoveryListener mDiscoveryListener = new MsDiscoveryListener() {
        @Override
        public void onDiscover(@NonNull final MediaServer server) {
            mHandler.post(() -> onDiscoverServer(server));
        }

        @Override
        public void onLost(@NonNull final MediaServer server) {
            mHandler.post(() -> onLostServer(server));
        }
    };

    private void onDiscoverServer(MediaServer server) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (mMsControlPoint.getDeviceListSize()
                != mServerListAdapter.getItemCount() + 1) {
            mServerListAdapter.clear();
            mServerListAdapter.addAll(mMsControlPoint.getDeviceList());
            mServerListAdapter.notifyDataSetChanged();
        } else {
            final int position = mServerListAdapter.add(server);
            mServerListAdapter.notifyItemInserted(position);
        }
    }

    private void onLostServer(MediaServer server) {
        final int position = mServerListAdapter.remove(server);
        if (position >= 0) {
            if (mMsControlPoint.getDeviceListSize()
                    == mServerListAdapter.getItemCount()) {
                mServerListAdapter.notifyItemRemoved(position);
            } else {
                mServerListAdapter.clear();
                mServerListAdapter.addAll(mMsControlPoint.getDeviceList());
                mServerListAdapter.notifyDataSetChanged();
            }
        }
        if (server.equals(mSelectedServer)) {
            mServerListAdapter.clearSelection();
            removeDetailFragment();
            mServerListAdapter.clearSelection();
            mSelectedServer = null;
        }
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.defaultStatusBar));
        }
        setContentView(R.layout.server_list_activity);
        mHandler = new Handler();
        mLan = Lan.createInstance(this);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        mServerListAdapter = new ServerListAdapter(this, mMsControlPoint.getDeviceList());
        mServerListAdapter.setOnItemClickListener(mOnItemClickListener);
        mNetworkAvailable = mLan.hasAvailableInterface();
        synchronized (mAvCpManager) {
            if (mNetworkAvailable) {
                if (savedInstanceState == null) {
                    mAvCpManager.initialize(mLan.getAvailableInterfaces());
                    mAvCpManager.start();
                }
            } else {
                mAvCpManager.terminate();
            }
        }
        registerReceiver(mConnectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        assert mSwipeRefreshLayout != null;
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.progress1, R.color.progress2, R.color.progress3, R.color.progress4);
        mSwipeRefreshLayout.setRefreshing(mServerListAdapter.getItemCount() == 0);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!mLan.hasAvailableInterface()) {
                return;
            }
            synchronized (mAvCpManager) {
                mAvCpManager.stop();
                mAvCpManager.terminate();
                mServerListAdapter.clear();
                mServerListAdapter.notifyDataSetChanged();
                mAvCpManager.initialize(mLan.getAvailableInterfaces());
                mAvCpManager.start();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.serverList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mServerListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));

        if (findViewById(R.id.serverDetailContainer) != null) {
            mTwoPane = true;
        }
        if (savedInstanceState != null) {
            final String udn = savedInstanceState.getString(KEY_SELECTED_SERVER_UDN);
            mSelectedServer = mMsControlPoint.getDevice(udn);
            mHasReenterTransition = savedInstanceState.getBoolean(KEY_HAS_REENTER_TRANSITION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mConnectivityReceiver);
        if (isFinishing()) {
            if (mSelectedServer != null) {
                mSelectedServer.unsubscribe();
            }
            synchronized (mAvCpManager) {
                mAvCpManager.terminate();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        removeDetailFragment();
        mServerListAdapter.clearSelection();
        super.onSaveInstanceState(outState);
        if (mSelectedServer != null) {
            outState.putString(KEY_SELECTED_SERVER_UDN, mSelectedServer.getUdn());
        }
        outState.putBoolean(KEY_HAS_REENTER_TRANSITION, mHasReenterTransition);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMsControlPoint.setMsDiscoveryListener(mDiscoveryListener);
        final List<MediaServer> list = mMsControlPoint.getDeviceList();
        final int position = list.indexOf(mSelectedServer);
        if (position >= 0 && !mTwoPane) {
            if (mHasReenterTransition && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        sharedElements.clear();
                        final int p = mServerListAdapter.indexOf(mSelectedServer);
                        final View shared = mRecyclerView.getLayoutManager().findViewByPosition(p);
                        if (shared != null) {
                            sharedElements.put(Const.SHARE_ELEMENT_NAME_DEVICE_ICON,
                                    shared.findViewById(R.id.accent));
                        }
                    }
                });
                getWindow().getSharedElementExitTransition().addListener(new TransitionListenerAdapter() {
                    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        updateListAdapter(list, position);
                        transition.removeListener(this);
                    }
                });
                mHasReenterTransition = false;
                return;
            }
            updateListAdapter(list, position);
            return;
        }
        updateListAdapter(list, position);
        if (position < 0) {
            removeDetailFragment();
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        sharedElements.clear();
                    }
                });
            }
        } else {
            mServerDetailFragment = ServerDetailFragment.newInstance(mSelectedServer.getUdn());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.serverDetailContainer, mServerDetailFragment)
                    .commit();
        }
    }

    private void updateListAdapter(final @NonNull List<MediaServer> list, final int position) {
        mServerListAdapter.clear();
        mServerListAdapter.addAll(list);
        mServerListAdapter.notifyDataSetChanged();
        mServerListAdapter.setSelection(position);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMsControlPoint.setMsDiscoveryListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSearchThread = new SearchThread();
        mSearchThread.start();
        if (!mLan.hasAvailableInterface()) {
            showToast(R.string.no_available_network);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSearchThread.shutdownRequest();
        mSearchThread = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(SettingsActivity.makeIntent(this));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    private class SearchThread extends Thread {
        public void shutdownRequest() {
            interrupt();
        }

        @Override
        public void run() {
            try {
                while (!interrupted()) {
                    synchronized (mAvCpManager) {
                        if (mAvCpManager.isInitialized()) {
                            mAvCpManager.search();
                        }
                    }
                    Thread.sleep(5000);
                }
            } catch (final InterruptedException ignored) {
            }
        }
    }
}
