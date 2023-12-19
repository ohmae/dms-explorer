/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.tabs

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object OpenUriUtils {
    private var defaultBrowserPackage: String? = null
    private var browserPackages: Set<String>? = null

    internal fun getBrowserPackages(
        context: Context,
        update: Boolean = false,
    ): Set<String> {
        if (!update) {
            browserPackages?.let {
                return it
            }
        }
        return getBrowserPackagesInner(context).also {
            browserPackages = it
        }
    }

    private fun getBrowserPackagesInner(context: Context): Set<String> {
        val pm = context.packageManager
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL else 0
        val activities = pm.queryIntentActivities(makeBrowserTestIntent(), flags)
        if (activities.isEmpty()) {
            return emptySet()
        }
        return activities.map { it.activityInfo.packageName }.toSet()
    }

    internal fun getDefaultBrowserPackage(
        context: Context,
        update: Boolean = false,
    ): String? {
        if (!update) {
            defaultBrowserPackage?.let {
                return it
            }
        }
        return getDefaultBrowserPackageInner(context).also {
            defaultBrowserPackage = it
        }
    }

    private fun getDefaultBrowserPackageInner(context: Context): String? {
        val pm = context.packageManager
        val browserInfo = pm.resolveActivity(makeBrowserTestIntent(), 0)
        if (browserInfo?.activityInfo == null) {
            return null
        }
        val packageName = browserInfo.activityInfo.packageName
        return if (getBrowserPackages(context).contains(packageName)) {
            packageName
        } else {
            null
        }
    }

    private fun makeBrowseIntent(uri: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        return intent
    }

    private fun makeBrowserTestIntent(): Intent = makeBrowseIntent("http://www.example.com/")

    fun hasDefaultAppOtherThanBrowser(
        context: Context,
        uri: String,
    ): Boolean {
        val pm = context.packageManager
        val intent = makeBrowseIntent(uri)
        val defaultApp = pm.resolveActivity(intent, 0)
        if (defaultApp?.activityInfo == null) {
            return false
        }
        val packageName = defaultApp.activityInfo.packageName
        if (getBrowserPackages(context).contains(packageName)) {
            return false
        }
        return pm.queryIntentActivities(intent, 0)
            .find { it.activityInfo != null && packageName == it.activityInfo.packageName } != null
    }
}
