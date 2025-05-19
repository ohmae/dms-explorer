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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mm2d.log.DefaultSender
import net.mm2d.log.Logger

@Suppress("unused")
class DebugApp : App() {
    override fun initializeOverrideWhenDebug() {
        setUpLogger()
        setUpStrictMode()
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

    private fun makeThreadPolicy(): ThreadPolicy =
        ThreadPolicy.Builder().apply {
            detectAll()
            penaltyLog()
            penaltyDeathOnNetwork()
        }.build()

    private fun makeVmPolicy(): VmPolicy =
        VmPolicy.Builder().apply {
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
}
