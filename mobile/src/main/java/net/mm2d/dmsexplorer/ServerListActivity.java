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
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.mm2d.android.cds.MediaServer;
import net.mm2d.android.cds.MsControlPoint;
import net.mm2d.android.cds.MsControlPoint.MsDiscoveryListener;
import net.mm2d.android.widget.DividerItemDecoration;
import net.mm2d.dmsexplorer.ServerListAdapter.OnItemClickListener;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
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
    private final MsControlPoint mMsControlPoint = mDataHolder.getMsControlPoint();
    private MediaServer mSelectedServer;
    private ServerDetailFragment mServerDetailFragment;
    private ServerListAdapter mServerListAdapter;
    private ConnectivityManager mConnectivityManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean available = hasAvailableNetwork();
            if (mNetworkAvailable != available) {
                synchronized (mMsControlPoint) {
                    if (available) {
                        mMsControlPoint.initialize(getWifiInterface());
                        mMsControlPoint.start();
                    } else {
                        mMsControlPoint.stop();
                        mMsControlPoint.terminate();
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
        public void onItemClick(View v, View accent, int position, MediaServer server) {
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
                mServerListAdapter.setSelection(position);
                mSelectedServer = server;
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
        }
    };

    private boolean hasAvailableNetwork() {
        final NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
        return ni != null && ni.isConnected()
                && (ni.getType() == ConnectivityManager.TYPE_WIFI
                || ni.getType() == ConnectivityManager.TYPE_ETHERNET);
    }

    private Collection<NetworkInterface> getWifiInterface() {
        final NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()
                || ni.getType() != ConnectivityManager.TYPE_WIFI) {
            return null;
        }
        final WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        final WifiInfo wi = wm.getConnectionInfo();
        if (wi == null) {
            return null;
        }
        int ip = wi.getIpAddress();
        final byte[] addr = new byte[4];
        addr[0] = (byte) (ip & 0xff);
        ip >>= 8;
        addr[1] = (byte) (ip & 0xff);
        ip >>= 8;
        addr[2] = (byte) (ip & 0xff);
        ip >>= 8;
        addr[3] = (byte) (ip & 0xff);
        final InetAddress ipaddr;
        try {
            ipaddr = InetAddress.getByAddress(addr);
        } catch (final UnknownHostException ignored) {
            return null;
        }
        final Enumeration<NetworkInterface> nis;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            return null;
        }
        while (nis.hasMoreElements()) {
            final NetworkInterface nif = nis.nextElement();
            try {
                if (nif.isLoopback()
                        || nif.isPointToPoint()
                        || nif.isVirtual()
                        || !nif.isUp()) {
                    continue;
                }
                final List<InterfaceAddress> ifas = nif.getInterfaceAddresses();
                for (final InterfaceAddress a : ifas) {
                    if (a.getAddress().equals(ipaddr)) {
                        final Collection<NetworkInterface> c = new ArrayList<>();
                        c.add(nif);
                        return c;
                    }
                }
            } catch (final SocketException ignored) {
            }
        }
        return null;
    }

    private final MsDiscoveryListener mDiscoveryListener = new MsDiscoveryListener() {
        @Override
        public void onDiscover(@NonNull final MediaServer server) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onDiscoverServer(server);
                }
            });
        }

        @Override
        public void onLost(@NonNull final MediaServer server) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLostServer(server);
                }
            });
        }
    };

    private void onDiscoverServer(MediaServer server) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (mMsControlPoint.getMediaServerListSize()
                != mServerListAdapter.getItemCount() + 1) {
            mServerListAdapter.clear();
            mServerListAdapter.addAll(mMsControlPoint.getMediaServerList());
            mServerListAdapter.notifyDataSetChanged();
        } else {
            final int position = mServerListAdapter.add(server);
            mServerListAdapter.notifyItemInserted(position);
        }
    }

    private void onLostServer(MediaServer server) {
        final int position = mServerListAdapter.remove(server);
        if (position >= 0) {
            if (mMsControlPoint.getMediaServerListSize()
                    == mServerListAdapter.getItemCount()) {
                mServerListAdapter.notifyItemRemoved(position);
            } else {
                mServerListAdapter.clear();
                mServerListAdapter.addAll(mMsControlPoint.getMediaServerList());
                mServerListAdapter.notifyDataSetChanged();
            }
        }
        if (mTwoPane && server.equals(mSelectedServer)) {
            if (mServerDetailFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(mServerDetailFragment)
                        .commit();
                mServerDetailFragment = null;
                mServerListAdapter.setSelection(-1);
                mSelectedServer = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_server_list);
        mHandler = new Handler();
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        mMsControlPoint.setMsDiscoveryListener(mDiscoveryListener);
        mServerListAdapter = new ServerListAdapter(this, mMsControlPoint.getMediaServerList());
        mServerListAdapter.setOnItemClickListener(mOnItemClickListener);
        mNetworkAvailable = hasAvailableNetwork();
        synchronized (mMsControlPoint) {
            if (mNetworkAvailable) {
                if (savedInstanceState == null) {
                    mMsControlPoint.initialize(getWifiInterface());
                    mMsControlPoint.start();
                }
            } else {
                mMsControlPoint.terminate();
            }
        }
        registerReceiver(mConnectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        assert mSwipeRefreshLayout != null;
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.progress1, R.color.progress2, R.color.progress3, R.color.progress4);
        mSwipeRefreshLayout.setRefreshing(mServerListAdapter.getItemCount() == 0);
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!hasAvailableNetwork()) {
                    return;
                }
                synchronized (mMsControlPoint) {
                    mMsControlPoint.stop();
                    mMsControlPoint.terminate();
                    mServerListAdapter.clear();
                    mServerListAdapter.notifyDataSetChanged();
                    mMsControlPoint.initialize(getWifiInterface());
                    mMsControlPoint.start();
                }
            }
        });
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.server_list);
        assert recyclerView != null;
        recyclerView.setAdapter(mServerListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));

        if (findViewById(R.id.server_detail_container) != null) {
            mTwoPane = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mConnectivityReceiver);
        if (isFinishing()) {
            synchronized (mMsControlPoint) {
                mMsControlPoint.setMsDiscoveryListener(null);
                mMsControlPoint.terminate();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSearchThread = new SearchThread();
        mSearchThread.start();
        if (!hasAvailableNetwork()) {
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
                    synchronized (mMsControlPoint) {
                        if (mMsControlPoint.isInitialized()) {
                            mMsControlPoint.search();
                        }
                    }
                    Thread.sleep(5000);
                }
            } catch (final InterruptedException ignored) {
            }
        }
    }
}
