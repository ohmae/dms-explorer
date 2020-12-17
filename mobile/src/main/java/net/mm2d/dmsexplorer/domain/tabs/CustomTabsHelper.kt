/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.tabs

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.*
import androidx.core.os.bundleOf
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsHelper(
    private val context: Context
) : CustomTabsServiceConnection() {
    private var bound: Boolean = false
    var session: CustomTabsSession? = null
        private set

    internal fun bind() {
        if (!bound) {
            val packageName = findPackageNameToUse(context) ?: return
            bound = CustomTabsClient.bindCustomTabsService(context, packageName, this)
        }
    }

    internal fun unbind() {
        if (bound) {
            context.unbindService(this)
            bound = false
            session = null
        }
    }

    fun mayLaunchUrl(url: String) {
        session?.mayLaunchUrl(Uri.parse(url), null, null)
    }

    fun mayLaunchUrl(urls: List<String>) {
        val session = session ?: return
        if (urls.isEmpty()) {
            return
        }
        try {
            if (urls.size == 1) {
                session.mayLaunchUrl(Uri.parse(urls[0]), null, null)
                return
            }
            val otherLikelyBundles = urls.subList(1, urls.size).map {
                bundleOf(CustomTabsService.KEY_URL to Uri.parse(it))
            }
            session.mayLaunchUrl(Uri.parse(urls[0]), null, otherLikelyBundles)
        } catch (ignored: Exception) {
            unbind()
        }
    }

    override fun onCustomTabsServiceConnected(
        name: ComponentName,
        client: CustomTabsClient
    ) {
        try {
            client.warmup(0)
            session = client.newSession(CustomTabsCallback())
        } catch (ignored: Exception) {
            unbind()
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        session = null
    }

    companion object {
        private val PREFERRED_PACKAGES = listOf(
            "com.android.chrome", // Chrome
            "com.chrome.beta", // Chrome Beta
            "com.chrome.dev", // Chrome Dev
            "com.chrome.canary"   // Chrome Canary
        )
        private const val ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService"
        private const val EXTRA_CUSTOM_TABS_KEEP_ALIVE =
            "android.support.customtabs.extra.KEEP_ALIVE"

        var packageNameToBind: String? = null
            private set

        fun addKeepAliveExtra(
            context: Context,
            intent: Intent
        ) {
            val keepAliveIntent = Intent().setClassName(
                context.packageName, KeepAliveService::class.java.canonicalName!!
            )
            intent.putExtra(EXTRA_CUSTOM_TABS_KEEP_ALIVE, keepAliveIntent)
        }

        private fun findPackageNameToUse(context: Context): String? {
            packageNameToBind = findPackageNameToUseInner(context)
            return packageNameToBind
        }

        private fun findPackageNameToUseInner(context: Context): String? {
            val pm = context.packageManager
            val browsers = OpenUriUtils.getBrowserPackages(context)
            val services = pm.queryIntentServices(Intent(ACTION_CUSTOM_TABS_CONNECTION), 0)
            val candidate = ArrayList<String>()
            for (service in services) {
                if (service.serviceInfo == null) {
                    continue
                }
                val packageName = service.serviceInfo.packageName
                if (browsers.contains(packageName)) {
                    candidate.add(packageName)
                }
            }
            if (candidate.isEmpty()) {
                return null
            }
            if (candidate.size == 1) {
                return candidate[0]
            }
            val defaultBrowser = OpenUriUtils.getDefaultBrowserPackage(context)
            if (candidate.contains(defaultBrowser)) {
                return defaultBrowser
            }
            for (packageName in PREFERRED_PACKAGES) {
                if (candidate.contains(packageName)) {
                    return packageName
                }
            }
            return null
        }
    }
}
