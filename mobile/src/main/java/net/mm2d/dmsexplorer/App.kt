/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer

import android.app.Application
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mm2d.dmsexplorer.debug.DebugData
import net.mm2d.dmsexplorer.domain.AppRepository
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.update.UpdateChecker
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter
import net.mm2d.log.DefaultSender
import net.mm2d.log.Logger

/**
 * Log出力変更のための継承。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        setUpLogger()
        setUpStrictMode()
        RxJavaPlugins.setErrorHandler { logError(it) }
        DebugData.initialize(this)
        Settings.initialize(this)
        EventRouter.initialize(this)
        EventLogger.initialize(this)
        EventLogger.sendDailyLog()
        Repository.set(AppRepository(this))
        UpdateChecker().check()
    }

    private fun logError(e: Throwable) {
        when (e) {
            is UndeliverableException
            -> Logger.w(e.cause, "UndeliverableException:")
            is OnErrorNotImplementedException
            -> Logger.w(e.cause, "OnErrorNotImplementedException:")
            else
            -> Logger.w(e)
        }
    }

    private fun setUpLogger() {
        if (BuildConfig.DEBUG) {
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
    }

    private fun setUpStrictMode() {
        if (BuildConfig.DEBUG) {
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
        } else {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX)
            StrictMode.setVmPolicy(VmPolicy.LAX)
        }
    }
}
