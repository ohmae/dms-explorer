/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import net.mm2d.android.net.Lan
import net.mm2d.android.upnp.AvControlPointManager
import net.mm2d.android.upnp.avt.MediaRenderer
import net.mm2d.android.upnp.avt.MrControlPoint
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.android.upnp.cds.MsControlPoint
import net.mm2d.android.upnp.cds.MsControlPoint.MsDiscoveryListener
import net.mm2d.android.util.RuntimeEnvironment
import net.mm2d.android.util.Toaster
import net.mm2d.dmsexplorer.BuildConfig
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.debug.DebugData
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ControlPointModel(
    context: Context,
    private val selectMediaServerObserver: (MediaServer?) -> Unit,
    private val selectMediaRendererObserver: (MediaRenderer?) -> Unit,
) {
    private var wifiLock: WifiManager.WifiLock? = null
    private val avControlPointManager = AvControlPointManager()
    private val applicationContext: Context = context.applicationContext
    private val lan: Lan = Lan.createInstance(applicationContext)
    private val initialized = AtomicBoolean()
    private var searchThread: SearchThread? = null
    var selectedMediaServer: MediaServer? = null
        set(server) {
            selectedMediaServer?.unsubscribe()
            field = server
            selectMediaServerObserver.invoke(server)
            selectedMediaServer?.subscribe()
        }
    private var selectedMediaRenderer: MediaRenderer? = null
    private var msDiscoveryListener = MS_DISCOVERY_LISTENER

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            if (!initializeOrTerminate(lan.hasAvailableInterface())) {
                Toaster.show(applicationContext, R.string.no_available_network)
            }
        }
    }

    private val msControlPoint: MsControlPoint
        get() = avControlPointManager.msControlPoint

    val mrControlPoint: MrControlPoint
        get() = avControlPointManager.mrControlPoint

    val numberOfMediaServer: Int
        get() = msControlPoint.deviceListSize

    val mediaServerList: List<MediaServer>
        get() = msControlPoint.deviceList

    private inner class SearchThread : Thread() {
        @Volatile
        private var shutdownRequest: Boolean = false

        fun shutdownRequest() {
            interrupt()
            shutdownRequest = true
        }

        override fun run() {
            try {
                while (!shutdownRequest) {
                    search()
                    sleep(SEARCH_INTERVAL)
                }
            } catch (ignored: InterruptedException) {
            }
        }
    }

    private fun search() {
        synchronized(avControlPointManager) {
            if (!avControlPointManager.isInitialized) {
                return
            }
            if (BuildConfig.DEBUG && RuntimeEnvironment.isEmulator) {
                for (location in DebugData.getPinnedDeviceLocationList()) {
                    avControlPointManager.addPinnedDevice(location)
                }
            }
            avControlPointManager.search()
        }
    }

    fun setMsDiscoveryListener(
        listener: MsDiscoveryListener?,
    ) {
        msDiscoveryListener = listener ?: MS_DISCOVERY_LISTENER
    }

    fun clearSelectedServer() {
        selectedMediaServer = null
    }

    fun isSelectedMediaServer(
        server: MediaServer,
    ): Boolean = selectedMediaServer != null && selectedMediaServer == server

    fun setSelectedMediaRenderer(
        server: MediaRenderer?,
    ) {
        selectedMediaRenderer?.unsubscribe()
        selectedMediaRenderer = server
        selectMediaRendererObserver.invoke(server)
        selectedMediaRenderer?.subscribe()
    }

    fun clearSelectedRenderer() {
        setSelectedMediaRenderer(null)
    }

    @SuppressLint("WifiManagerPotentialLeak")
    private fun acquireWifiLock() {
        val lock = wifiLock
        if (lock == null) {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG).also {
                it.setReferenceCounted(true)
                it.acquire()
            }
        } else {
            lock.acquire()
        }
    }

    private fun releaseWifiLock() {
        wifiLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wifiLock = null
    }

    fun initialize() {
        if (!initialized.getAndSet(true)) {
            applicationContext.registerReceiver(
                connectivityReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            )
            acquireWifiLock()
        }
    }

    fun terminate() {
        selectedMediaServer = null
        synchronized(avControlPointManager) {
            avControlPointManager.stop()
            avControlPointManager.terminate()
        }
        if (initialized.getAndSet(false)) {
            applicationContext.unregisterReceiver(connectivityReceiver)
            releaseWifiLock()
        }
    }

    private fun initializeOrTerminate(
        initialize: Boolean,
    ): Boolean {
        synchronized(avControlPointManager) {
            if (initialize) {
                val interfaces = lan.getAvailableInterfaces()
                if (!interfaces.isEmpty()) {
                    avControlPointManager.initialize(interfaces)
                    avControlPointManager.start()
                    return true
                }
            }
            avControlPointManager.stop()
            avControlPointManager.terminate()
            return false
        }
    }

    @Synchronized
    fun searchStart() {
        if (searchThread != null) {
            searchStop()
        }
        msControlPoint.setMsDiscoveryListener(msDiscoveryListener)
        searchThread = SearchThread().also { it.start() }
        if (!lan.hasAvailableInterface()) {
            Toaster.show(applicationContext, R.string.no_available_network)
        }
    }

    @Synchronized
    fun searchStop() {
        searchThread?.shutdownRequest()
        searchThread = null
        msControlPoint.setMsDiscoveryListener(null)
    }

    fun restart(
        callback: (() -> Unit)? = null,
    ) {
        if (!lan.hasAvailableInterface()) {
            Toaster.show(applicationContext, R.string.no_available_network)
            return
        }
        val interfaces = lan.getAvailableInterfaces()
        if (interfaces.isEmpty()) {
            Toaster.show(applicationContext, R.string.no_available_network)
            return
        }
        synchronized(avControlPointManager) {
            avControlPointManager.stop()
            avControlPointManager.terminate()
            callback?.invoke()
            avControlPointManager.initialize(interfaces)
            avControlPointManager.start()
        }
    }

    companion object {
        private val TAG = ControlPointModel::class.java.simpleName
        private val SEARCH_INTERVAL = TimeUnit.SECONDS.toMillis(5)
        private val MS_DISCOVERY_LISTENER: MsDiscoveryListener = object : MsDiscoveryListener {
            override fun onDiscover(
                server: MediaServer,
            ) = Unit

            override fun onLost(
                server: MediaServer,
            ) = Unit
        }
    }
}
