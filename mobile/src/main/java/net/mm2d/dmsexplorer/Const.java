/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Const {
    public static final String PACKAGE_NAME = "net.mm2d.dmsexplorer";

    // Broadcast
    private static final String PREFIX = PACKAGE_NAME + ".";
    public static final String ACTION_PLAY = PREFIX + "ACTION_PLAY";
    public static final String ACTION_NEXT = PREFIX + "ACTION_NEXT";
    public static final String ACTION_PREV = PREFIX + "ACTION_PREV";

    public static final String KEY_HAS_TOOLBAR_COLOR = "KEY_HAS_TOOLBAR_COLOR";
    public static final String KEY_TOOLBAR_EXPANDED_COLOR = "KEY_TOOLBAR_EXPANDED_COLOR";
    public static final String KEY_TOOLBAR_COLLAPSED_COLOR = "KEY_TOOLBAR_COLLAPSED_COLOR";

    public static final String SHARE_ELEMENT_NAME_DEVICE_ICON = "SHARE_ELEMENT_NAME_DEVICE_ICON";

    public static final String URL_UPDATE_BASE = "https://ohmae.github.io/DmsExplorer/";
    public static final String URL_UPDATE_PATH = "json/update.json";
    public static final String URL_GITHUB_PROJECT = "https://github.com/ohmae/DmsExplorer";
    public static final String URL_PRIVACY_POLICY = "https://github.com/ohmae/DmsExplorer/blob/develop/PRIVACY-POLICY.md";
    public static final String URL_OPEN_SOURCE_LICENSE = "file:///android_asset/license.html";

    public static final int REQUEST_CODE_ACTION_PLAY = 1;
    public static final int REQUEST_CODE_ACTION_NEXT = 2;
    public static final int REQUEST_CODE_ACTION_PREVIOUS = 3;
}
