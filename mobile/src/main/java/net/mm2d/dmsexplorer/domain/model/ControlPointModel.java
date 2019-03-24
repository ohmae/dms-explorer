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
import android.net.wifi.WifiManager;

import net.mm2d.android.net.Lan;
import net.mm2d.android.upnp.AvControlPointManager;
import net.mm2d.android.upnp.avt.MediaRenderer;
import net.mm2d.android.upnp.avt.MrControlPoint;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.android.upnp.cds.MsControlPoint;
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener;
import net.mm2d.android.util.RuntimeEnvironment;
import net.mm2d.android.util.Toaster;
import net.mm2d.dmsexplorer.BuildConfig;
import net.mm2d.dmsexplorer.R;
import net.mm2d.dmsexplorer.debug.DebugData;

import java.net.NetworkInterface;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private static final String TAG = ControlPointModel.class.getSimpleName();
    @Nullable
    private WifiManager.WifiLock mWifiLock;
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
    @Nullable
    private SearchThread mSearchThread;
    @Nullable
    private MediaServer mSelectedMediaServer;
    @Nullable
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
        public void onReceive(
                final Context context,
                final Intent intent) {
            if (!initializeOrTerminate(mLan.hasAvailableInterface())) {
                Toaster.show(mContext, R.string.no_available_network);
            }
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
                    search();
                    Thread.sleep(SEARCH_INTERVAL);
                }
            } catch (final InterruptedException ignored) {
            }
        }
    }

    private void search() {
        synchronized (mAvControlPointManager) {
            if (!mAvControlPointManager.isInitialized()) {
                return;
            }
            if (BuildConfig.DEBUG && RuntimeEnvironment.isEmulator()) {
                for (final String location : DebugData.getPinnedDeviceLocationList()) {
                    mAvControlPointManager.addPinnedDevice(location);
                }
            }
            mAvControlPointManager.search();
        }
    }

    public ControlPointModel(
            @NonNull final Context context,
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

    private void acquireWifiLock() {
        if (mWifiLock == null) {
            final WifiManager wm = (WifiManager) mContext.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
            mWifiLock.setReferenceCounted(true);
        }
        mWifiLock.acquire();
    }

    private void releaseWifiLock() {
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        mWifiLock = null;
    }

    public void initialize() {
        if (!mInitialized.getAndSet(true)) {
            mContext.registerReceiver(mConnectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            acquireWifiLock();
        }
    }

    public void terminate() {
        setSelectedMediaServer(null);
        synchronized (mAvControlPointManager) {
            mAvControlPointManager.stop();
            mAvControlPointManager.terminate();
        }
        if (mInitialized.getAndSet(false)) {
            mContext.unregisterReceiver(mConnectivityReceiver);
            releaseWifiLock();
        }
    }

    private boolean initializeOrTerminate(final boolean initialize) {
        synchronized (mAvControlPointManager) {
            if (initialize) {
                final Collection<NetworkInterface> interfaces = mLan.getAvailableInterfaces();
                if (!interfaces.isEmpty()) {
                    mAvControlPointManager.initialize(interfaces);
                    mAvControlPointManager.start();
                    return true;
                }
            }
            mAvControlPointManager.stop();
            mAvControlPointManager.terminate();
            return false;
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
            Toaster.show(mContext, R.string.no_available_network);
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

    @NonNull
    public List<MediaServer> getMediaServerList() {
        return getMsControlPoint().getDeviceList();
    }

    public interface TerminateCallback {
        void callback();
    }

    public void restart(@Nullable final TerminateCallback callback) {
        if (!mLan.hasAvailableInterface()) {
            Toaster.show(mContext, R.string.no_available_network);
            return;
        }
        final Collection<NetworkInterface> interfaces = mLan.getAvailableInterfaces();
        if (interfaces.isEmpty()) {
            Toaster.show(mContext, R.string.no_available_network);
            return;
        }
        synchronized (mAvControlPointManager) {
            mAvControlPointManager.stop();
            mAvControlPointManager.terminate();
            if (callback != null) {
                callback.callback();
            }
            mAvControlPointManager.initialize(interfaces);
            mAvControlPointManager.start();
        }
    }
}
