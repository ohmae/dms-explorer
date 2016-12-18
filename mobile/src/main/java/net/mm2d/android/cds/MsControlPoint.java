/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
 * MediaServerのControlPoint機能。
 *
 * <p>ControlPointは継承しておらず、MediaServerとしてのインターフェースのみを提供する。
 * UPnPのActionを直接叩く必要がある場合は、getControlPoint
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class MsControlPoint {
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
     * ContainerUpdateIdsのsubscribeイベントを通知するリスナー。
     */
    public interface ContainerUpdateIdsListener {
        /**
         * ContainerUpdateIdsが通知されたときにコールされる。
         *
         * @param server イベントを発行したMediaServer
         * @param ids    更新のあったID
         */
        void onContainerUpdateIds(@NonNull MediaServer server, @NonNull List<String> ids);
    }

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscover(@NonNull Device device) {
            discoverDevice(device);
        }

        @Override
        public void onLost(@NonNull Device device) {
            lostDevice(device);
        }
    };
    private final NotifyEventListener mNotifyEventListener = new NotifyEventListener() {
        @Override
        public void onNotifyEvent(@NonNull Service service, long seq, String variable, String value) {
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

    private ControlPoint mControlPoint;
    private boolean mInitialized = false;
    private final Map<String, MediaServer> mMediaServerMap;
    private MsDiscoveryListener mMsDiscoveryListener;
    private ContainerUpdateIdsListener mContainerUpdateIdsListener;

    /**
     * インスタンス作成。
     */
    public MsControlPoint() {
        mMediaServerMap = Collections.synchronizedMap(new LinkedHashMap<String, MediaServer>());
    }

    /**
     * ラップしているControlPointのインスタンスを返す。
     *
     * <p>取扱い注意！
     * このクラスが提供していない機能を利用する場合に必要となるため、
     * 取得インターフェースを用意しているが、
     * 外部で直接操作することを想定していないため、
     * 利用する場合は必ずこのクラスの実装を理解した上で使用すること。
     *
     * @return ControlPoint
     */
    @Nullable
    public ControlPoint getControlPoint() {
        return mControlPoint;
    }

    /**
     * MediaServerのファクトリーメソッド。
     *
     * @param device Device
     * @return MediaServer
     */
    protected MediaServer createMediaServer(Device device) {
        return new MediaServer(device);
    }

    private void discoverDevice(@NonNull Device device) {
        if (!device.getDeviceType().startsWith(Cds.MS_DEVICE_TYPE)) {
            return;
        }
        final MediaServer server = createMediaServer(device);
        mMediaServerMap.put(server.getUdn(), server);
        if (mMsDiscoveryListener != null) {
            mMsDiscoveryListener.onDiscover(server);
        }
    }

    private void lostDevice(@NonNull Device device) {
        final MediaServer server = getMediaServer(device.getUdn());
        if (server == null) {
            return;
        }
        if (mMsDiscoveryListener != null) {
            mMsDiscoveryListener.onLost(server);
        }
        mMediaServerMap.remove(server.getUdn());
    }

    /**
     * 機器発見の通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    public void setMsDiscoveryListener(@Nullable MsDiscoveryListener listener) {
        mMsDiscoveryListener = listener;
    }

    /**
     * ContainerUpdateIdsの通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    public void setContainerUpdateIdsListener(@Nullable ContainerUpdateIdsListener listener) {
        mContainerUpdateIdsListener = listener;
    }

    /**
     * 保持しているMediaServerの個数を返す。
     *
     * @return MediaServerの個数
     */
    public int getMediaServerListSize() {
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
    public List<MediaServer> getMediaServerList() {
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
    public MediaServer getMediaServer(@Nullable String udn) {
        return mMediaServerMap.get(udn);
    }

    /**
     * SSDP Searchを実行する。
     *
     * Searchパケットを一つ投げるのみであり、定期的に実行するにはアプリ側での実装が必要。
     */
    public void search() {
        if (!mInitialized) {
            throw new IllegalStateException("ControlPoint is not initialized");
        }
        mControlPoint.search();
    }

    /**
     * 初期化が完了しているか。
     *
     * @return 初期化完了していればtrue
     */
    public boolean isInitialized() {
        return mInitialized;
    }

    /**
     * 初期化する。
     */
    public void initialize() {
        initialize(null);
    }

    /**
     * 初期化する。
     *
     * @param interfaces 使用するインターフェース
     */
    public void initialize(@Nullable Collection<NetworkInterface> interfaces) {
        if (mInitialized) {
            terminate();
        }
        mInitialized = true;
        mControlPoint = new ControlPoint(interfaces);
        mControlPoint.addDiscoveryListener(mDiscoveryListener);
        mControlPoint.addNotifyEventListener(mNotifyEventListener);
        mControlPoint.initialize();
    }

    /**
     * 処理を開始する。
     */
    public void start() {
        if (!mInitialized) {
            throw new IllegalStateException("ControlPoint is not initialized");
        }
        mControlPoint.start();
    }

    /**
     * 処理を終了する。
     */
    public void stop() {
        if (!mInitialized) {
            throw new IllegalStateException("ControlPoint is not initialized");
        }
        mControlPoint.stop();
    }

    /**
     * 終了する。
     */
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
