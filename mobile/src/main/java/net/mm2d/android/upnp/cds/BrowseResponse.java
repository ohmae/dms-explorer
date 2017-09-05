/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.support.annotation.NonNull;

import net.mm2d.util.TextParseUtils;

import java.util.Map;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class BrowseResponse {
    private static final String RESULT = "Result";
    private static final String NUMBER_RETURNED = "NumberReturned";
    private static final String TOTAL_MATCHES = "TotalMatches";
    private static final String UPDATE_ID = "UpdateID";
    private final Map<String, String> mResult;

    BrowseResponse(@NonNull final Map<String, String> result) {
        mResult = result;
    }

    int getNumberReturned() {
        return TextParseUtils.parseIntSafely(mResult.get(NUMBER_RETURNED), -1);
    }

    int getTotalMatches() {
        return TextParseUtils.parseIntSafely(mResult.get(TOTAL_MATCHES), -1);
    }

    int getUpdateId() {
        return TextParseUtils.parseIntSafely(mResult.get(UPDATE_ID), -1);
    }

    String getResult() {
        return mResult.get(RESULT);
    }
}
