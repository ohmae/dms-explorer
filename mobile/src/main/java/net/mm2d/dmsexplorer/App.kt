/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.multidex.MultiDexApplication
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import net.mm2d.dmsexplorer.debug.DebugData
import net.mm2d.dmsexplorer.domain.AppRepository
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.update.UpdateChecker
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter
import net.mm2d.log.Logger
import net.mm2d.log.android.AndroidSenders

/**
 * Log出力変更のための継承。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Logger.setLogLevel(Logger.VERBOSE)
            Logger.setSender(AndroidSenders.create())
            AndroidSenders.appendCaller(true)
            AndroidSenders.appendThread(true)
        }
        setStrictMode()
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
            -> Logger.w("UndeliverableException:", e.cause)
            is OnErrorNotImplementedException
            -> Logger.w("OnErrorNotImplementedException:", e.cause)
            else
            -> Logger.w(e)
        }
    }

    private fun setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
        } else {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX)
            StrictMode.setVmPolicy(VmPolicy.LAX)
        }
    }
}
