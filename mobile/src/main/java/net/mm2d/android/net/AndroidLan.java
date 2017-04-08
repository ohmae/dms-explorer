/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.util.NetworkUtils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 実機環境でのLAN接続情報を扱うクラス。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class AndroidLan extends Lan {
    @NonNull
    private final WifiManager mWifiManager;
    @NonNull
    private final ConnectivityManager mConnectivityManager;

    /**
     * インスタンス作成。
     *
     * @param context コンストラクタ
     */
    AndroidLan(@NonNull final Context context) {
        final Context appContext = context.getApplicationContext();
        mWifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean hasAvailableInterface() {
        final NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
        return ni != null && ni.isConnected()
                && (ni.getType() == ConnectivityManager.TYPE_WIFI
                || ni.getType() == ConnectivityManager.TYPE_ETHERNET);
    }

    @Nullable
    @Override
    public Collection<NetworkInterface> getAvailableInterfaces() {
        if (!hasWifiConnection()) {
            return null;
        }
        final InetAddress address = getWifiInetAddress();
        if (address == null) {
            return null;
        }
        final List<NetworkInterface> netIfList = NetworkUtils.getNetworkInterfaceList();
        for (final NetworkInterface netIf : netIfList) {
            if (hasAddress(netIf, address)) {
                return Collections.singletonList(netIf);
            }
        }
        return null;
    }

    private static boolean hasAddress(@NonNull final NetworkInterface netIf,
                                      @NonNull final InetAddress targetAddress) {
        final List<InterfaceAddress> addressList = netIf.getInterfaceAddresses();
        for (final InterfaceAddress address : addressList) {
            if (address.getAddress().equals(targetAddress)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWifiConnection() {
        final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    @Nullable
    private InetAddress getWifiInetAddress() {
        final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return null;
        }
        try {
            return InetAddress.getByAddress(intToByteArray(wifiInfo.getIpAddress()));
        } catch (final UnknownHostException ignored) {
        }
        return null;
    }

    @NonNull
    private byte[] intToByteArray(int ip) {
        final byte[] array = new byte[4];
        array[0] = (byte) (ip & 0xff);
        ip >>= 8;
        array[1] = (byte) (ip & 0xff);
        ip >>= 8;
        array[2] = (byte) (ip & 0xff);
        ip >>= 8;
        array[3] = (byte) (ip & 0xff);
        return array;
    }
}
