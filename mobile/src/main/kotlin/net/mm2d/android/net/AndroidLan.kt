/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import net.mm2d.upnp.util.NetworkUtils
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * 実機環境でのLAN接続情報を扱うクラス。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class AndroidLan(
    context: Context,
) : Lan() {
    private val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun getAvailableInterfaces(): Collection<NetworkInterface> =
        if (hasAvailableInterface()) {
            NetworkUtils.getNetworkInterfaceList().filter { isUsableInterface(it) }
        } else {
            emptyList()
        }

    override fun hasAvailableInterface(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                    ) ||
                (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 &&
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)
                    )
        }
        val ni = connectivityManager.activeNetworkInfo ?: return false
        return ni.isConnected &&
            (
                ni.type == ConnectivityManager.TYPE_WIFI ||
                    ni.type == ConnectivityManager.TYPE_ETHERNET
                )
    }

    private fun isUsableInterface(
        netIf: NetworkInterface,
    ): Boolean =
        if (netIf.isUp && netIf.supportsMulticast() && !netIf.isLoopback) {
            runCatching { netIf.interfaceAddresses.any { it.address is Inet4Address } }
                .getOrDefault(false)
        } else {
            false
        }
}
