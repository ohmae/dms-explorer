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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
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
import net.mm2d.android.widget.DividerItemDecoration;
import net.mm2d.dmsexplorer.ServerListAdapter.OnItemClickListener;

import java.util.List;

/**
 * MediaServerのサーチ、選択を行うActivity。
 *
 * <p>アプリ起動時最初に表示されるActivity
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class ServerListActivity extends AppCompatActivity {
    private static final String TAG = "ServerListActivity";
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
    private Lan mLan;

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
        public void onItemClick(final @NonNull View v, final @NonNull View accent,
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
                        .replace(R.id.server_detail_container, mServerDetailFragment)
                        .commit();
            } else {
                final Context context = v.getContext();
                final Intent intent = ServerDetailActivity.makeIntent(context, server.getUdn());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent, ActivityOptions
                            .makeSceneTransitionAnimation(ServerListActivity.this, accent, "share")
                            .toBundle());
                } else {
                    startActivity(intent);
                }
            }
            mServerListAdapter.setSelection(position);
            mSelectedServer = server;
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
        setContentView(R.layout.act_server_list);
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
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.server_list);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mServerListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));

        if (findViewById(R.id.server_detail_container) != null) {
            mTwoPane = true;
        }
        if (savedInstanceState != null) {
            final String udn = savedInstanceState.getString(Const.EXTRA_SERVER_UDN);
            mSelectedServer = mMsControlPoint.getDevice(udn);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mConnectivityReceiver);
        if (isFinishing()) {
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
            outState.putString(Const.EXTRA_SERVER_UDN, mSelectedServer.getUdn());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMsControlPoint.setMsDiscoveryListener(mDiscoveryListener);
        final List<MediaServer> list = mMsControlPoint.getDeviceList();
        mServerListAdapter.clear();
        mServerListAdapter.addAll(list);
        mServerListAdapter.notifyDataSetChanged();
        final int position = list.indexOf(mSelectedServer);
        if (position < 0) {
            removeDetailFragment();
            mServerListAdapter.clearSelection();
        } else {
            if (mTwoPane) {
                mServerDetailFragment = ServerDetailFragment.newInstance(mSelectedServer.getUdn());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.server_detail_container, mServerDetailFragment)
                        .commit();
            }
            mServerListAdapter.setSelection(position);
        }
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
