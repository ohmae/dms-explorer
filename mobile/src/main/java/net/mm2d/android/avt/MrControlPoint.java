/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.avt;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.ControlPointWrapper;
import net.mm2d.upnp.ControlPoint;
import net.mm2d.upnp.ControlPoint.DiscoveryListener;
import net.mm2d.upnp.ControlPoint.NotifyEventListener;
import net.mm2d.upnp.Device;
import net.mm2d.upnp.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MediaRenderer用のControlPointインターフェース
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MrControlPoint implements ControlPointWrapper {
    /**
     * 機器発見のイベントを通知するリスナー。
     */
    public interface MrDiscoveryListener {
        /**
         * 機器発見時に通知される。
         *
         * @param server 発見したMediaRenderer
         */
        void onDiscover(@NonNull MediaRenderer server);

        /**
         * 機器喪失時に通知される。
         *
         * @param server 喪失したMediaRenderer
         */
        void onLost(@NonNull MediaRenderer server);
    }

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscover(final @NonNull Device device) {
            discoverDevice(device);
        }

        @Override
        public void onLost(final @NonNull Device device) {
            lostDevice(device);
        }
    };

    private final NotifyEventListener mNotifyEventListener = new NotifyEventListener() {
        @Override
        public void onNotifyEvent(@NonNull Service service, long seq,
                                  @NonNull String variable, @NonNull String value) {
        }
    };
    private boolean mInitialized = false;
    private final Map<String, MediaRenderer> mMediaRendererMap;
    @Nullable
    private MrDiscoveryListener mMrDiscoveryListener;
    @Nullable
    private ExecutorService mExecutorService;

    public MrControlPoint() {
        mMediaRendererMap = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    /**
     * MediaRendererのファクトリーメソッド。
     *
     * @param device Device
     * @return MediaRenderer
     */
    @NonNull
    protected MediaRenderer createMediaRenderer(final @NonNull Device device) {
        return new MediaRenderer(this, device);
    }

    private void discoverDevice(final @NonNull Device device) {
        if (!device.getDeviceType().startsWith(Avt.MR_DEVICE_TYPE)
                || device.findServiceById(Avt.AVT_SERVICE_ID) == null) {
            return;
        }
        final MediaRenderer renderer = createMediaRenderer(device);
        mMediaRendererMap.put(renderer.getUdn(), renderer);
        if (mMrDiscoveryListener != null) {
            mMrDiscoveryListener.onDiscover(renderer);
        }
    }

    private void lostDevice(final @NonNull Device device) {
        final MediaRenderer renderer = mMediaRendererMap.remove(device.getUdn());
        if (renderer == null) {
            return;
        }
        if (mMrDiscoveryListener != null) {
            mMrDiscoveryListener.onLost(renderer);
        }
    }

    /**
     * 機器発見の通知リスナーを登録する。
     *
     * @param listener リスナー
     */
    public void setMrDiscoveryListener(final @Nullable MrDiscoveryListener listener) {
        mMrDiscoveryListener = listener;
    }

    /**
     * 保持しているMediaRendererの個数を返す。
     *
     * @return MediaRendererの個数
     */
    @Override
    public int getDeviceListSize() {
        return mMediaRendererMap.size();
    }

    /**
     * MediaServerのリストを返す。
     *
     * 内部Mapのコピーを返すため使用注意。
     *
     * @return MediaRendererのリスト。
     */
    @NonNull
    @Override
    public List<MediaRenderer> getDeviceList() {
        synchronized (mMediaRendererMap) {
            return new ArrayList<>(mMediaRendererMap.values());
        }
    }

    /**
     * 指定UDNに対応したMediaServerを返す。
     *
     * @param udn UDN
     * @return MediaRenderer、見つからない場合null
     */
    @Nullable
    @Override
    public MediaRenderer getDevice(final @Nullable String udn) {
        return mMediaRendererMap.get(udn);
    }

    /**
     * 初期化する。
     *
     * @param controlPoint ControlPoint
     */
    @Override
    public void initialize(final @NonNull ControlPoint controlPoint) {
        if (mInitialized) {
            terminate(controlPoint);
        }
        mExecutorService = Executors.newSingleThreadExecutor();
        mMediaRendererMap.clear();
        controlPoint.addDiscoveryListener(mDiscoveryListener);
        controlPoint.addNotifyEventListener(mNotifyEventListener);
    }

    /**
     * 終了する。
     *
     * @param controlPoint ControlPoint
     */
    @Override
    public void terminate(final @NonNull ControlPoint controlPoint) {
        if (!mInitialized) {
            return;
        }
        controlPoint.removeDiscoveryListener(mDiscoveryListener);
        controlPoint.removeNotifyEventListener(mNotifyEventListener);
        mMediaRendererMap.clear();
        mExecutorService.shutdownNow();
    }

    void invoke(final @NonNull Runnable runnable) {
        mExecutorService.execute(runnable);
    }
}
