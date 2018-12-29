/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.net

import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.NonNull
import net.mm2d.util.NetworkUtils
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

/**
 * 実機環境でのLAN接続情報を扱うクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class AndroidLan(context: Context) : Lan() {
    private val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun getAvailableInterfaces(): Collection<NetworkInterface> {
        if (!hasAvailableInterface()) {
            return emptyList()
        }
        return NetworkUtils.getNetworkInterfaceList()
            .filter { isUsableInterface(it) }
    }

    override fun hasAvailableInterface(): Boolean {
        val ni = connectivityManager.activeNetworkInfo ?: return false
        return ni.isConnected
                && (ni.type == ConnectivityManager.TYPE_WIFI
                || ni.type == ConnectivityManager.TYPE_ETHERNET)
    }

    private fun isUsableInterface(@NonNull netIf: NetworkInterface): Boolean {
        try {
            if (!netIf.isUp || !netIf.supportsMulticast() || netIf.isLoopback) {
                return false
            }
            return netIf.interfaceAddresses.find { it.address is Inet4Address } == null
        } catch (ignored: SocketException) {
        }
        return false
    }
}
