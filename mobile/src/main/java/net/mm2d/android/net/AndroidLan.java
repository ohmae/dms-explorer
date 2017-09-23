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
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import net.mm2d.util.NetworkUtils;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
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

    @NonNull
    @Override
    public Collection<NetworkInterface> getAvailableInterfaces() {
        if (!hasAvailableInterface()) {
            return Collections.emptyList();
        }
        final List<NetworkInterface> result = new ArrayList<>();
        for (final NetworkInterface netIf : NetworkUtils.getNetworkInterfaceList()) {
            if (isUsableInterface(netIf)) {
                result.add(netIf);
            }
        }
        return result;
    }

    private boolean isUsableInterface(@NonNull final NetworkInterface netIf) {
        try {
            if (!netIf.isUp()) {
                return false;
            }
            if (!netIf.supportsMulticast()) {
                return false;
            }
            if (netIf.isLoopback()) {
                return false;
            }
            final List<InterfaceAddress> addresses = netIf.getInterfaceAddresses();
            for (final InterfaceAddress address : addresses) {
                if (address.getAddress() instanceof Inet4Address) {
                    return true;
                }
            }
        } catch (final SocketException ignored) {
        }
        return false;
    }
}
