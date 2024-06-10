/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.webkit.URLUtil
import androidx.browser.customtabs.CustomTabsIntent
import net.mm2d.android.util.LaunchUtils
import net.mm2d.dmsexplorer.R
import net.mm2d.dmsexplorer.domain.tabs.CustomTabsHelper
import net.mm2d.dmsexplorer.domain.tabs.OpenUriUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class OpenUriCustomTabsModel(
    private val helper: CustomTabsHelper,
    private val themeModel: ThemeModel,
) : OpenUriModel {
    private var useCustomTabs: Boolean = false

    override fun setUseCustomTabs(use: Boolean) {
        useCustomTabs = use
    }

    override fun openUri(context: Context, uri: String) {
        if (!useCustomTabs ||
            !URLUtil.isNetworkUrl(uri) ||
            OpenUriUtils.hasDefaultAppOtherThanBrowser(context, uri)
        ) {
            LaunchUtils.openUri(context, uri)
            return
        }
        if (!openUriOnCustomTabs(context, uri)) {
            LaunchUtils.openUri(context, uri)
        }
    }

    override fun mayLaunchUrl(url: String) {
        helper.mayLaunchUrl(url)
    }

    override fun mayLaunchUrl(urls: List<String>) {
        helper.mayLaunchUrl(urls)
    }

    private fun openUriOnCustomTabs(context: Context, uri: String): Boolean {
        val packageNameToBind = CustomTabsHelper.packageNameToBind
        if (packageNameToBind.isNullOrEmpty()) {
            return false
        }
        val customTabsIntent = CustomTabsIntent.Builder(helper.session)
            .setShowTitle(true)
            .setToolbarColor(getToolbarColor(context))
            .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
            .build()
        CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent)
        customTabsIntent.intent.setPackage(packageNameToBind)
        try {
            customTabsIntent.launchUrl(context, Uri.parse(uri))
        } catch (ignored: ActivityNotFoundException) {
            return false
        }
        return true
    }

    private fun getToolbarColor(context: Context): Int {
        if (context !is Activity) {
            return DEFAULT_TOOLBAR_COLOR
        }
        val color = themeModel.getToolbarColor(context)
        return if (color != 0) color else DEFAULT_TOOLBAR_COLOR
    }

    companion object {
        private const val DEFAULT_TOOLBAR_COLOR = Color.BLACK
    }
}
