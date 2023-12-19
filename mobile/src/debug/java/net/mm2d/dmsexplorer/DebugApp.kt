/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer

import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.leakcanary2.FlipperLeakEventListener
import com.facebook.flipper.plugins.leakcanary2.LeakCanary2FlipperPlugin
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import leakcanary.LeakCanary
import net.mm2d.log.DefaultSender
import net.mm2d.log.Logger

@Suppress("unused")
class DebugApp : App() {
    override fun initializeOverrideWhenDebug() {
        setUpLogger()
        setUpStrictMode()
        setUpFlipper()
    }

    private fun setUpLogger() {
        Logger.setLogLevel(Logger.DEBUG)
        Logger.setSender(
            DefaultSender.create { level, tag, message ->
                GlobalScope.launch(Dispatchers.Main) {
                    message.split("\n").forEach {
                        Log.println(level, tag, it)
                    }
                }
            },
        )
        DefaultSender.appendThread(true)
    }

    private fun setUpStrictMode() {
        StrictMode.setThreadPolicy(makeThreadPolicy())
        StrictMode.setVmPolicy(makeVmPolicy())
    }

    private fun makeThreadPolicy(): ThreadPolicy = ThreadPolicy.Builder().apply {
        detectAll()
        penaltyLog()
        penaltyDeathOnNetwork()
    }.build()

    private fun makeVmPolicy(): VmPolicy = VmPolicy.Builder().apply {
        penaltyLog()
        detectActivityLeaks()
        detectLeakedClosableObjects()
        detectLeakedRegistrationObjects()
        detectFileUriExposure()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            detectCleartextNetwork()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            penaltyDeathOnFileUriExposure()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            detectContentUriWithoutPermission()
            // detectUntaggedSockets()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            detectCredentialProtectedWhileLocked()
        }
    }.build()

    private fun setUpFlipper() {
        LeakCanary.config = LeakCanary.config.run {
            copy(eventListeners = eventListeners + FlipperLeakEventListener())
        }
        SoLoader.init(this, false)
        if (!FlipperUtils.shouldEnableFlipper(this)) return
        val client = AndroidFlipperClient.getInstance(this)
        client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
        client.addPlugin(NavigationFlipperPlugin.getInstance())
        client.addPlugin(NetworkFlipperPlugin())
        client.addPlugin(DatabasesFlipperPlugin(this))
        client.addPlugin(SharedPreferencesFlipperPlugin(this))
        client.addPlugin(LeakCanary2FlipperPlugin())
        client.addPlugin(CrashReporterPlugin.getInstance())
        client.start()
    }
}
