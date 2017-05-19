/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.net.Lan;
import net.mm2d.android.upnp.AvControlPointManager;
import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.upnp.cds.MsControlPoint;
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener;
import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.R;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ControlPointModel {
    public interface SelectMediaServerObserver {
        void update(@Nullable MediaServer server);
    }

    public interface SelectMediaRendererObserver {
        void update(@Nullable MediaRenderer renderer);
    }

    @NonNull
    private final SelectMediaServerObserver mSelectMediaServerObserver;
    @NonNull
    private final SelectMediaRendererObserver mSelectMediaRendererObserver;
    @NonNull
    private final AvControlPointManager mAvControlPointManager = new AvControlPointManager();
    @NonNull
    private final Context mContext;
    @NonNull
    private final Lan mLan;
    @NonNull
    private final AtomicBoolean mInitialized = new AtomicBoolean();
    private boolean mNetworkAvailable;
    private SearchThread mSearchThread;
    private MediaServer mSelectedMediaServer;
    private MediaRenderer mSelectedMediaRenderer;
    private static final MsDiscoveryListener MS_DISCOVERY_LISTENER = new MsDiscoveryListener() {
        @Override
        public void onDiscover(@NonNull final MediaServer server) {
        }

        @Override
        public void onLost(@NonNull final MediaServer server) {
        }
    };
    private MsDiscoveryListener mMsDiscoveryListener = MS_DISCOVERY_LISTENER;

    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean available = mLan.hasAvailableInterface();
            if (mNetworkAvailable != available) {
                initializeOrTerminate(available);
                if (!available) {
                    Toaster.showLong(mContext, R.string.no_available_network);
                }
            }
            mNetworkAvailable = available;
        }
    };

    private static final long SEARCH_INTERVAL = TimeUnit.SECONDS.toMillis(5);

    private class SearchThread extends Thread {
        private volatile boolean mShutdownRequest;

        void shutdownRequest() {
            interrupt();
            mShutdownRequest = true;
        }

        @Override
        public void run() {
            try {
                while (!mShutdownRequest) {
                    synchronized (mAvControlPointManager) {
                        if (mAvControlPointManager.isInitialized()) {
                            mAvControlPointManager.search();
                        }
                    }
                    Thread.sleep(SEARCH_INTERVAL);
                }
            } catch (final InterruptedException ignored) {
            }
        }
    }


    public ControlPointModel(@NonNull final Context context,
                             @NonNull final SelectMediaServerObserver serverObserver,
                             @NonNull final SelectMediaRendererObserver rendererObserver) {
        mContext = context.getApplicationContext();
        mLan = Lan.createInstance(mContext);
        mSelectMediaServerObserver = serverObserver;
        mSelectMediaRendererObserver = rendererObserver;
    }

    public void setMsDiscoveryListener(@Nullable final MsDiscoveryListener listener) {
        mMsDiscoveryListener = listener != null ? listener : MS_DISCOVERY_LISTENER;
    }

    public void setSelectedMediaServer(@Nullable final MediaServer server) {
        if (mSelectedMediaServer != null) {
            mSelectedMediaServer.unsubscribe();
        }
        mSelectedMediaServer = server;
        mSelectMediaServerObserver.update(server);
        if (mSelectedMediaServer != null) {
            mSelectedMediaServer.subscribe();
        }
    }

    public void clearSelectedServer() {
        setSelectedMediaServer(null);
    }

    @Nullable
    public MediaServer getSelectedMediaServer() {
        return mSelectedMediaServer;
    }

    public boolean isSelectedMediaServer(@NonNull final MediaServer server) {
        return mSelectedMediaServer != null && mSelectedMediaServer.equals(server);
    }

    public void setSelectedMediaRenderer(@Nullable final MediaRenderer server) {
        if (mSelectedMediaRenderer != null) {
            mSelectedMediaRenderer.unsubscribe();
        }
        mSelectedMediaRenderer = server;
        mSelectMediaRendererObserver.update(server);
        if (mSelectedMediaRenderer != null) {
            mSelectedMediaRenderer.subscribe();
        }
    }

    public void clearSelectedRenderer() {
        setSelectedMediaRenderer(null);
    }

    public void initialize() {
        mNetworkAvailable = mLan.hasAvailableInterface();
        initializeOrTerminate(mNetworkAvailable);
        if (!mInitialized.getAndSet(true)) {
            mContext.registerReceiver(mConnectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    public void terminate() {
        setSelectedMediaServer(null);
        initializeOrTerminate(false);
        if (mInitialized.getAndSet(false)) {
            mContext.unregisterReceiver(mConnectivityReceiver);
        }
    }

    private void initializeOrTerminate(boolean initialize) {
        synchronized (mAvControlPointManager) {
            if (initialize) {
                mAvControlPointManager.initialize(mLan.getAvailableInterfaces());
                mAvControlPointManager.start();
            } else {
                mAvControlPointManager.stop();
                mAvControlPointManager.terminate();
            }
        }
    }

    public synchronized void searchStart() {
        if (mSearchThread != null) {
            searchStop();
        }
        getMsControlPoint().setMsDiscoveryListener(mMsDiscoveryListener);
        mSearchThread = new SearchThread();
        mSearchThread.start();
        if (!mLan.hasAvailableInterface()) {
            Toaster.showLong(mContext, R.string.no_available_network);
        }
    }

    public synchronized void searchStop() {
        if (mSearchThread != null) {
            mSearchThread.shutdownRequest();
            mSearchThread = null;
        }
        getMsControlPoint().setMsDiscoveryListener(null);
    }

    @NonNull
    private MsControlPoint getMsControlPoint() {
        return mAvControlPointManager.getMsControlPoint();
    }

    @NonNull
    public MrControlPoint getMrControlPoint() {
        return mAvControlPointManager.getMrControlPoint();
    }

    public int getNumberOfMediaServer() {
        return getMsControlPoint().getDeviceListSize();
    }

    public List<MediaServer> getMediaServerList() {
        return getMsControlPoint().getDeviceList();
    }

    public interface TerminateCallback {
        void callback();
    }

    public void restart(@Nullable TerminateCallback callback) {
        if (!mLan.hasAvailableInterface()) {
            return;
        }
        synchronized (mAvControlPointManager) {
            mAvControlPointManager.stop();
            mAvControlPointManager.terminate();
            if (callback != null) {
                callback.callback();
            }
            mAvControlPointManager.initialize(mLan.getAvailableInterfaces());
            mAvControlPointManager.start();
        }
    }
}
