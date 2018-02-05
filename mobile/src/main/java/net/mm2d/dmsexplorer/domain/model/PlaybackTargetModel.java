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
import android.text.TextUtils;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.Tag;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class PlaybackTargetModel {
    @NonNull
    private final ContentEntity mContentEntity;
    @NonNull
    private final CdsObject mCdsObject;
    @Nullable
    private Tag mTargetRes;
    @NonNull
    private Uri mUri = Uri.EMPTY;
    @Nullable
    private String mMimeType;

    public PlaybackTargetModel(@NonNull final ContentEntity entity) {
        mContentEntity = entity;
        mCdsObject = (CdsObject) entity.getObject();
        mTargetRes = mCdsObject.getTag(CdsObject.RES);
        updateUri();
    }

    @NonNull
    public ContentEntity getContentEntity() {
        return mContentEntity;
    }

    public void setResIndex(final int index) {
        mTargetRes = mCdsObject.getTag(CdsObject.RES, index);
        updateUri();
    }

    private void updateUri() {
        if (mTargetRes == null || TextUtils.isEmpty(mTargetRes.getValue())) {
            mUri = Uri.EMPTY;
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

    @NonNull
    public Uri getUri() {
        return mUri;
    }

    @Nullable
    public String getMimeType() {
        return mMimeType;
    }

    public int getResCount() {
        return mContentEntity.getResourceCount();
    }

    public String[] createResChoices() {
        final List<Tag> tagList = mCdsObject.getTagList(CdsObject.RES);
        if (tagList == null) {
            return new String[0];
        }
        final List<String> itemList = new ArrayList<>();
        for (final Tag tag : tagList) {
            final String bitrate = tag.getAttribute(CdsObject.BITRATE);
            final String resolution = tag.getAttribute(CdsObject.RESOLUTION);
            final String protocolInfo = tag.getAttribute(CdsObject.PROTOCOL_INFO);
            final String mimeType = CdsObject.extractMimeTypeFromProtocolInfo(protocolInfo);
            final String protocol = CdsObject.extractProtocolFromProtocolInfo(protocolInfo);
            final StringBuilder sb = new StringBuilder();
            if (protocol != null) {
                sb.append(protocol);
            }
            if (mimeType != null) {
                if (sb.length() != 0) {
                    sb.append(" ");
                }
                sb.append(mimeType);
            }
            if (bitrate != null) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append("bitrate: ");
                sb.append(bitrate);
            }
            if (resolution != null) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append("resolution: ");
                sb.append(resolution);
            }
            itemList.add(sb.toString());
        }
        return itemList.toArray(new String[itemList.size()]);
    }
}
