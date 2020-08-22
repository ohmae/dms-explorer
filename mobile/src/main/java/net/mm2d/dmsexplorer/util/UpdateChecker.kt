package net.mm2d.dmsexplorer.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter

object UpdateChecker {
    private const val DAYS_FOR_UPDATE = 2

    fun check(context: Context) {
        val manager = AppUpdateManagerFactory.create(context.applicationContext)
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.clientVersionStalenessDays.let { it != null && it >= DAYS_FOR_UPDATE } &&
                    info.isImmediateUpdateAllowed
                ) {
                    EventRouter.notifyUpdateAvailability(true)
                }
            }
    }

    fun tryToUpdate(activity: Activity) {
        val manager = AppUpdateManagerFactory.create(activity.applicationContext)
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.clientVersionStalenessDays.let { it != null && it >= DAYS_FOR_UPDATE } &&
                    info.isImmediateUpdateAllowed
                ) {
                    val options = AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                    manager.startUpdateFlow(info, activity, options)
                } else {
                    EventRouter.notifyUpdateAvailability(false)
                }
            }
            .addOnFailureListener {
                EventRouter.notifyUpdateAvailability(false)
            }
    }
}
