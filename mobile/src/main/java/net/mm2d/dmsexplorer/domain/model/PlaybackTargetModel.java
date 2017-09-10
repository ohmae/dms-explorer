/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.Tag;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;
import net.mm2d.util.TextUtils;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PlaybackTargetModel {
    @NonNull
    private final CdsObject mCdsObject;
    @Nullable
    private Tag mTargetRes;
    @Nullable
    private Uri mUri;
    @Nullable
    private String mMimeType;

    public PlaybackTargetModel(@NonNull final ContentEntity entity) {
        mCdsObject = (CdsObject) entity.getObject();
        mTargetRes = mCdsObject.getTag(CdsObject.RES);
        updateUri();
    }

    @NonNull
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

    @NonNull
    public String getTitle() {
        return mCdsObject.getTitle();
    }

    @Nullable
    public Uri getUri() {
        return mUri;
    }

    @Nullable
    public String getUriString() {
        return mUri == null ? null : mUri.toString();
    }

    @Nullable
    public String getMimeType() {
        return mMimeType;
    }

    public int getResCount() {
        return mCdsObject.getResourceCount();
    }
}
