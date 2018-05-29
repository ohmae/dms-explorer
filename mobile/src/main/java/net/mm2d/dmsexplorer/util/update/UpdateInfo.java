/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update;

import com.squareup.moshi.Json;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public class UpdateInfo {
    private static class Mobile {
        @Json(name = "versionName")
        private String versionName;
        @Json(name = "versionCode")
        private int versionCode;
        @Json(name = "targetInclude")
        private int[] targetInclude;
        @Json(name = "targetExclude")
        private int[] targetExclude;
    }

    @Json(name = "mobile")
    private Mobile mobile;

    public boolean isValid() {
        return mobile != null
                && mobile.versionName != null
                && mobile.versionCode != 0
                && mobile.targetInclude != null
                && mobile.targetExclude != null;
    }

    public int getVersionCode() {
        return mobile.versionCode;
    }

    public String getVersionName() {
        return mobile.versionName;
    }

    public int[] getTargetInclude() {
        return mobile.targetInclude;
    }

    public int[] getTargetExclude() {
        return mobile.targetExclude;
    }
}
