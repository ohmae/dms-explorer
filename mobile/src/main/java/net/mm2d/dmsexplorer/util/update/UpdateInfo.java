/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update;

import com.google.gson.annotations.SerializedName;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public class UpdateInfo {
    @SerializedName("mobile")
    private final Mobile mMobile;

    public UpdateInfo(Mobile mobile) {
        mMobile = mobile;
    }

    public boolean isValid() {
        return mMobile != null
                && mMobile.versionName != null
                && mMobile.versionCode != 0
                && mMobile.targetInclude != null
                && mMobile.targetExclude != null;
    }

    public int getVersionCode() {
        return mMobile.versionCode;
    }

    public String getVersionName() {
        return mMobile.versionName;
    }

    public int[] getTargetInclude() {
        return mMobile.targetInclude;
    }

    public int[] getTargetExclude() {
        return mMobile.targetExclude;
    }

    private static class Mobile {
        @SerializedName("versionName")
        private String versionName;
        @SerializedName("versionCode")
        private int versionCode;
        @SerializedName("targetInclude")
        private int[] targetInclude;
        @SerializedName("targetExclude")
        private int[] targetExclude;
    }
}
