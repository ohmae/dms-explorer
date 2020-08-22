/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer

import android.app.Application
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import net.mm2d.dmsexplorer.debug.DebugData
import net.mm2d.dmsexplorer.domain.AppRepository
import net.mm2d.dmsexplorer.log.EventLogger
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.util.UpdateChecker
import net.mm2d.log.Logger

/**
 * Log出力変更のための継承。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
open class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initForDebug()
        RxJavaPlugins.setErrorHandler { logError(it) }
        DebugData.initialize(this)
        Settings.initialize(this)
        EventLogger.initialize(this)
        EventLogger.sendDailyLog()
        Repository.set(AppRepository(this))
        UpdateChecker.check(this)
    }

    open fun initForDebug() = Unit

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
}
