/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object Const {
    const val PACKAGE_NAME = "net.mm2d.dmsexplorer"

    // Broadcast
    private const val PREFIX = "$PACKAGE_NAME."
    const val ACTION_PLAY = PREFIX + "ACTION_PLAY"
    const val ACTION_NEXT = PREFIX + "ACTION_NEXT"
    const val ACTION_PREV = PREFIX + "ACTION_PREV"

    const val KEY_HAS_TOOLBAR_COLOR = "KEY_HAS_TOOLBAR_COLOR"
    const val KEY_TOOLBAR_EXPANDED_COLOR = "KEY_TOOLBAR_EXPANDED_COLOR"
    const val KEY_TOOLBAR_COLLAPSED_COLOR = "KEY_TOOLBAR_COLLAPSED_COLOR"

    const val SHARE_ELEMENT_NAME_DEVICE_ICON = "SHARE_ELEMENT_NAME_DEVICE_ICON"

    const val URL_UPDATE_BASE = "https://ohmae.github.io/DmsExplorer/"
    const val URL_UPDATE_PATH = "json/update.json"
    const val URL_GITHUB_PROJECT = "https://github.com/ohmae/DmsExplorer"
    const val URL_PRIVACY_POLICY = "https://github.com/ohmae/DmsExplorer/blob/develop/PRIVACY-POLICY.md"
    const val URL_OPEN_SOURCE_LICENSE = "file:///android_asset/license.html"

    const val REQUEST_CODE_ACTION_PLAY = 1
    const val REQUEST_CODE_ACTION_NEXT = 2
    const val REQUEST_CODE_ACTION_PREVIOUS = 3
}
