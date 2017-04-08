/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.net.Uri;
import android.support.annotation.NonNull;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.Tag;
import net.mm2d.util.TextUtils;

import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PlaybackTargetModel {
    private final CdsObject mCdsObject;
    private Tag mTargetRes;
    private Uri mUri;
    private String mMimeType;

    public PlaybackTargetModel(@NonNull final CdsObject object) {
        mCdsObject = object;
        mTargetRes = object.getTag(CdsObject.RES);
        updateUri();
    }

    public CdsObject getCdsObject() {
        return mCdsObject;
    }

    public void setResIndex(final int index) {
        mTargetRes = mCdsObject.getTag(CdsObject.RES, index);
        updateUri();
    }

    private void updateUri() {
        if (mTargetRes == null || TextUtils.isEmpty(mTargetRes.getValue())) {
            mUri = null;
            mMimeType = null;
            return;
        }
        mUri = Uri.parse(mTargetRes.getValue());
        final String protocolInfo = mTargetRes.getAttribute(CdsObject.PROTOCOL_INFO);
        mMimeType = CdsObject.extractMimeTypeFromProtocolInfo(protocolInfo);
    }

    public Uri getUri() {
        return mUri;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public int getResCount() {
        final List<Tag> list = mCdsObject.getTagList(CdsObject.RES);
        return list == null ? 0 : list.size();
    }
}
