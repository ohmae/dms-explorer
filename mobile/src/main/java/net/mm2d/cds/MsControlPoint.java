/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import net.mm2d.upnp.ControlPoint;
import net.mm2d.upnp.ControlPoint.DiscoveryListener;
import net.mm2d.upnp.ControlPoint.NotifyEventListener;
import net.mm2d.upnp.Device;
import net.mm2d.upnp.Service;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MsControlPoint {
    public interface MsDiscoveryListener {
        void onDiscover(MediaServer server);

        void onLost(MediaServer server);
    }

    public interface ContainerUpdateIdsListener {
        void onContainerUpdateIds(MediaServer server, List<String> ids);
    }

    private ControlPoint mControlPoint;
    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscover(Device device) {
            discoverDevice(device);
        }

        @Override
        public void onLost(Device device) {
            lostDevice(device);
        }
    };
    private final NotifyEventListener mNotifyEventListener = new NotifyEventListener() {
        @Override
        public void onNotifyEvent(Service service, long seq, String variable, String value) {
            if (mContainerUpdateIdsListener == null) {
                return;
            }
            if (!service.getServiceId().equals(Cds.CDS_SERVICE_ID)
                    || !variable.equals(Cds.CONTAINER_UPDATE_IDS)) {
                return;
            }
            final String[] values = value.split(",");
            if (values.length == 0 || values.length % 2 != 0) {
                return;
            }
            final List<String> ids = new ArrayList<>();
            for (int i = 0; i < values.length; i += 2) {
                ids.add(values[i]);
            }
            final String udn = service.getDevice().getUdn();
            final MediaServer server = getMediaServer(udn);
            mContainerUpdateIdsListener.onContainerUpdateIds(server, ids);
        }
    };

    private boolean mInitialized = false;
    private final Map<String, MediaServer> mMediaServerMap;
    private MsDiscoveryListener mMsDiscoveryListener;
    private ContainerUpdateIdsListener mContainerUpdateIdsListener;

    public MsControlPoint() {
        mMediaServerMap = Collections.synchronizedMap(new LinkedHashMap<String, MediaServer>());
    }

    public ControlPoint getControlPoint() {
        return mControlPoint;
    }

    private void discoverDevice(Device device) {
        if (!device.getDeviceType().startsWith(Cds.MS_DEVICE_TYPE)) {
            return;
        }
        final MediaServer server = new MediaServer(device);
        mMediaServerMap.put(server.getUdn(), server);
        if (mMsDiscoveryListener != null) {
            mMsDiscoveryListener.onDiscover(server);
        }
    }

    private void lostDevice(Device device) {
        final MediaServer server = getMediaServer(device.getUdn());
        if (server == null) {
            return;
        }
        if (mMsDiscoveryListener != null) {
            mMsDiscoveryListener.onLost(server);
        }
        mMediaServerMap.remove(server.getUdn());
    }

    public void setMsDiscoveryListener(MsDiscoveryListener listener) {
        mMsDiscoveryListener = listener;
    }

    public void setContainerUpdateIdsListener(ContainerUpdateIdsListener listener) {
        mContainerUpdateIdsListener = listener;
    }

    public int getMediaServerListSize() {
        return mMediaServerMap.size();
    }

    public List<MediaServer> getMediaServerList() {
        synchronized (mMediaServerMap) {
            return new ArrayList<>(mMediaServerMap.values());
        }
    }

    public MediaServer getMediaServer(String udn) {
        return mMediaServerMap.get(udn);
    }

    public void search() {
        mControlPoint.search();
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public void initialize() {
        initialize(null);
    }

    public void initialize(Collection<NetworkInterface> interfaces) {
        if (mInitialized) {
            terminate();
        }
        mInitialized = true;
        mControlPoint = new ControlPoint(interfaces);
        mControlPoint.addDiscoveryListener(mDiscoveryListener);
        mControlPoint.addNotifyEventListener(mNotifyEventListener);
        mControlPoint.initialize();
    }

    public void start() {
        mControlPoint.start();
    }

    public void stop() {
        mControlPoint.stop();
    }

    public void terminate() {
        if (!mInitialized) {
            return;
        }
        mInitialized = false;
        mControlPoint.terminate();
        mControlPoint.removeDiscoveryListener(mDiscoveryListener);
        mControlPoint.removeNotifyEventListener(mNotifyEventListener);
        mMediaServerMap.clear();
    }
}
