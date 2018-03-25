/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.ControlPointWrapper;
import net.mm2d.upnp.ControlPoint;
import net.mm2d.upnp.ControlPoint.DiscoveryListener;
import net.mm2d.upnp.ControlPoint.NotifyEventListener;
import net.mm2d.upnp.Device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MediaServerのControlPoint機能。
 *
 * <p>ControlPointは継承しておらず、MediaServerとしてのインターフェースのみを提供する。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MsControlPoint implements ControlPointWrapper {
    /**
     * 機器発見のイベントを通知するリスナー。
     */
    public interface MsDiscoveryListener {
        /**
         * 機器発見時に通知される。
         *
         * @param server 発見したMediaServer
         */
        void onDiscover(@NonNull MediaServer server);

        /**
         * 機器喪失時に通知される。
         *
         * @param server 喪失したMediaServer
         */
        void onLost(@NonNull MediaServer server);
    }

    /**
     * ContainerUpdateIDsのsubscribeイベントを通知するリスナー。
     */
    public interface ContainerUpdateIdsListener {
        /**
         * ContainerUpdateIDsが通知されたときにコールされる。
         *
         * @param server イベントを発行したMediaServer
         * @param ids    更新のあったID
         */
        void onContainerUpdateIds(
                @NonNull MediaServer server,
                @NonNull List<String> ids);
    }

    /**
     * SystemUpdateIDのsubscribeイベントを通知するリスナー。
     */
    public interface SystemUpdateIdListener {
        /**
         * SystemUpdateIDが通知されたときにコールされる。
         *
         * @param server イベントを発行したMediaServer
         * @param id     UpdateID
         */
        void onSystemUpdateId(
                @NonNull MediaServer server,
                @NonNull String id);
    }

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscover(@NonNull final Device device) {
            discoverDevice(device);
        }

        @Override
        public void onLost(@NonNull final Device device) {
            lostDevice(device);
        }
    };

    private final NotifyEventListener mNotifyEventListener = (service, seq, variable, value) -> {
        final String udn = service.getDevice().getUdn();
        final MediaServer server = getDevice(udn);
        if (server == null || !service.getServiceId().equals(Cds.CDS_SERVICE_ID)) {
            return;
        }
        if (variable.equals(Cds.CONTAINER_UPDATE_IDS)) {
            onNotifyContainerUpdateIds(server, value);
        } else if (variable.equals(Cds.SYSTEM_UPDATE_ID)) {
            onNotifySystemUpdateId(server, value);
        }
    };

    private void onNotifyContainerUpdateIds(
            @NonNull final MediaServer server,
            @NonNull final String value) {
        if (mContainerUpdateIdsListener == null) {
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
        mContainerUpdateIdsListener.onContainerUpdateIds(server, ids);
    }

    private void onNotifySystemUpdateId(
            @NonNull final MediaServer server,
            @NonNull final String value) {
        if (mSystemUpdateIdListener == null) {
            return;
        }
        mSystemUpdateIdListener.onSystemUpdateId(server, value);
    }

    @NonNull
    private final AtomicBoolean mInitialized = new AtomicBoolean();
    @NonNull
    private final Map<String, MediaServer> mMediaServerMap;
    @Nullable
    private MsDiscoveryListener mMsDiscoveryListener;
    @Nullable
    private ContainerUpdateIdsListener mContainerUpdateIdsListener;
    @Nullable
    private SystemUpdateIdListener mSystemUpdateIdListener;

    /**
     * インスタンス作成。
     */
    public MsControlPoint() {
        mMediaServerMap = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    /**
     * MediaServerのファクトリーメソッド。
     *
     * @param device Device
     * @return MediaServer
     */
    protected MediaServer createMediaServer(@NonNull final Device device) {
        return new MediaServer(device);
    }

    private void discoverDevice(@NonNull final Device device) {
        if (!device.getDeviceType().startsWith(Cds.MS_DEVICE_TYPE)) {
            return;
        }
        final MediaServer server;
        try {
            server = createMediaServer(device);
        } catch (final IllegalArgumentException ignored) {
            return;
        }
        mMediaServerMap.put(server.getUdn(), server);
        if (mMsDiscoveryListener != null) {
            mMsDiscoveryListener.onDiscover(server);
        }
    }

    private void lostDevice(@NonNull final Device device) {
        final MediaServer server = mMediaServerMap.remove(device.getUdn());
        if (server == null) {
            return;
        }
        if (mMsDiscoveryListener != null) {
            mMsDiscoveryListener.onLost(server);
        }
    }

    /**
     * 機器発見の通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    public void setMsDiscoveryListener(@Nullable final MsDiscoveryListener listener) {
        mMsDiscoveryListener = listener;
    }

    /**
     * ContainerUpdateIdsの通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    public void setContainerUpdateIdsListener(@Nullable final ContainerUpdateIdsListener listener) {
        mContainerUpdateIdsListener = listener;
    }

    /**
     * SystemUpdateIdの通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    public void setSystemUpdateIdListener(@NonNull final SystemUpdateIdListener listener) {
        mSystemUpdateIdListener = listener;
    }

    /**
     * 保持しているMediaServerの個数を返す。
     *
     * @return MediaServerの個数
     */
    @Override
    public int getDeviceListSize() {
        return mMediaServerMap.size();
    }

    /**
     * MediaServerのリストを返す。
     *
     * 内部Mapのコピーを返すため使用注意。
     *
     * @return MediaServerのリスト。
     */
    @NonNull
    @Override
    public List<MediaServer> getDeviceList() {
        synchronized (mMediaServerMap) {
            return new ArrayList<>(mMediaServerMap.values());
        }
    }

    /**
     * 指定UDNに対応したMediaServerを返す。
     *
     * @param udn UDN
     * @return MediaServer、見つからない場合null
     */
    @Nullable
    @Override
    public MediaServer getDevice(@Nullable final String udn) {
        return mMediaServerMap.get(udn);
    }

    /**
     * 初期化する。
     *
     * @param controlPoint ControlPoint
     */
    @Override
    public void initialize(@NonNull final ControlPoint controlPoint) {
        if (mInitialized.get()) {
            terminate(controlPoint);
        }
        mInitialized.set(true);
        mMediaServerMap.clear();
        controlPoint.addDiscoveryListener(mDiscoveryListener);
        controlPoint.addNotifyEventListener(mNotifyEventListener);
    }

    /**
     * 終了する。
     *
     * @param controlPoint ControlPoint
     */
    @Override
    public void terminate(@NonNull final ControlPoint controlPoint) {
        if (!mInitialized.getAndSet(false)) {
            return;
        }
        controlPoint.removeDiscoveryListener(mDiscoveryListener);
        controlPoint.removeNotifyEventListener(mNotifyEventListener);
        mMediaServerMap.clear();
    }
}
