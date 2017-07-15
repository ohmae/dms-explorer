/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.util.update;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class UpdateInfo {
    private final int mVersionCode;
    @NonNull
    private final String mVersionName;
    @NonNull
    private final int[] mTargetInclude;
    @NonNull
    private final int[] mTargetExclude;

    UpdateInfo(@NonNull final JSONObject json) throws JSONException {
        final JSONObject mobile = json.getJSONObject("mobile");
        mVersionCode = mobile.getInt("versionCode");
        mVersionName = mobile.getString("versionName");
        final JSONArray include = mobile.getJSONArray("targetInclude");
        mTargetInclude = new int[include.length()];
        for (int i = 0; i < include.length(); i++) {
            mTargetInclude[i] = include.getInt(i);
        }
        final JSONArray exclude = mobile.getJSONArray("targetExclude");
        mTargetExclude = new int[exclude.length()];
        for (int i = 0; i < exclude.length(); i++) {
            mTargetExclude[i] = exclude.getInt(i);
        }
    }

    int getVersionCode() {
        return mVersionCode;
    }

    @NonNull
    String getVersionName() {
        return mVersionName;
    }

    @NonNull
    int[] getTargetInclude() {
        return mTargetInclude;
    }

    @NonNull
    int[] getTargetExclude() {
        return mTargetExclude;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof UpdateInfo)) {
            return false;
        }
        final UpdateInfo info = (UpdateInfo) obj;
        return mVersionCode == info.mVersionCode
                && mVersionName.equals(info.mVersionName)
                && Arrays.equals(mTargetInclude, info.mTargetInclude)
                && Arrays.equals(mTargetExclude, info.mTargetExclude);
    }
}
