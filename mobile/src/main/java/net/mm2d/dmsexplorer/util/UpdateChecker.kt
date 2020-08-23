package net.mm2d.dmsexplorer.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
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
        try {
            AppUpdateManagerFactory.create(context.applicationContext)
                .appUpdateInfo
                .addOnSuccessListener {
                    if (it.updateAvailable()) {
                        EventRouter.notifyUpdateAvailable(true)
                    }
                }
        } catch (ignored: Exception) {
        }
    }

    fun tryToUpdate(activity: Activity) {
        try {
            val manager = AppUpdateManagerFactory.create(activity.applicationContext)
            manager.appUpdateInfo
                .addOnSuccessListener {
                    if (it.updateAvailable()) {
                        val options = AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                        manager.startUpdateFlow(it, activity, options)
                    } else {
                        EventRouter.notifyUpdateAvailable(false)
                    }
                }
                .addOnFailureListener {
                    EventRouter.notifyUpdateAvailable(false)
                }
        } catch (ignored: Exception) {
        }
    }

    private fun AppUpdateInfo.updateAvailable(): Boolean =
        updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            clientVersionStalenessDays.let { it != null && it >= DAYS_FOR_UPDATE } &&
            isImmediateUpdateAllowed
}
