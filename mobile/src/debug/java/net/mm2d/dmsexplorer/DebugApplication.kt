/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mm2d.dmsexplorer.util.OkHttpClientHolder
import net.mm2d.log.DefaultSender
import net.mm2d.log.Logger

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class DebugApplication : App() {
    override fun initForDebug() {
        setUpLogger()
        setUpStrictMode()
        setUpStetho()
    }

    private fun setUpLogger() {
        Logger.setLogLevel(Logger.DEBUG)
        Logger.setSender(DefaultSender.create { level, tag, message ->
            GlobalScope.launch(Dispatchers.Main) {
                message.split("\n").forEach {
                    Log.println(level, tag, it)
                }
            }
        })
        DefaultSender.appendThread(true)
    }

    private fun setUpStrictMode() {
        StrictMode.enableDefaults()
        val vmPolicyBuilder = VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectActivityLeaks()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectFileUriExposure()
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            vmPolicyBuilder.detectContentUriWithoutPermission()
        }
        StrictMode.setVmPolicy(vmPolicyBuilder.build())
    }

    private fun setUpStetho() {
        Stetho.initializeWithDefaults(this)
        OkHttpClientHolder.addNetworkInterceptor(StethoInterceptor())
    }
}
