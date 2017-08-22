/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.BrowseResult;
import net.mm2d.android.upnp.cds.BrowseResult.StatusListener;
import net.mm2d.android.upnp.cds.CdsObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ContentDirectoryEntity implements StatusListener {
    private static final String ROOT_OBJECT_ID = "0";
    private static final String ROOT_TITLE = "";
    @NonNull
    private final String mParentId;
    @NonNull
    private final String mParentTitle;
    @Nullable
    private CdsObject mSelectedObject;
    @NonNull
    private List<CdsObject> mList = Collections.emptyList();
    @Nullable
    private BrowseResult mBrowseResult;
    private static final EntryListener ENTRY_LISTENER = (result, inProgress) -> {
    };
    @NonNull
    private EntryListener mEntryListener = ENTRY_LISTENER;

    public interface EntryListener {
        void onUpdate(
                @NonNull List<CdsObject> list,
                boolean inProgress);
    }

    public ContentDirectoryEntity() {
        this(ROOT_OBJECT_ID, ROOT_TITLE);
    }

    public ContentDirectoryEntity(
            @NonNull final String parentId,
            @NonNull final String parentTitle) {
        mParentId = parentId;
        mParentTitle = parentTitle;
    }

    public void terminate() {
        setEntryListener(null);
        if (mBrowseResult != null) {
            mBrowseResult.setStatusListener(null);
            mBrowseResult.cancel(true);
        }
    }

    @NonNull
    public String getParentId() {
        return mParentId;
    }

    @NonNull
    public String getParentTitle() {
        return mParentTitle;
    }

    public boolean isInProgress() {
        return mBrowseResult == null || !mBrowseResult.isDone();
    }

    @NonNull
    public List<CdsObject> getList() {
        return mList;
    }

    @Nullable
    public ContentDirectoryEntity enterChild(@NonNull final CdsObject object) {
        if (!mList.contains(object)) {
            return null;
        }
        if (!object.isContainer()) {
            return null;
        }
        mSelectedObject = object;
        return new ContentDirectoryEntity(object.getObjectId(), object.getTitle());
    }

    public void setSelectedObject(@Nullable final CdsObject object) {
        if (!mList.contains(object)) {
            mSelectedObject = null;
            return;
        }
        mSelectedObject = object;
    }

    @Nullable
    public CdsObject getSelectedObject() {
        return mSelectedObject;
    }

    public void setEntryListener(@Nullable final EntryListener listener) {
        mEntryListener = listener != null ? listener : ENTRY_LISTENER;
    }

    public void clearState() {
        mSelectedObject = null;
        mList = Collections.emptyList();
        if (mBrowseResult != null) {
            mBrowseResult.setStatusListener(null);
            mBrowseResult.cancel(true);
            mBrowseResult = null;
        }
    }

    public void setBrowseResult(@NonNull final BrowseResult result) {
        mBrowseResult = result;
        mBrowseResult.setStatusListener(this);
    }

    @Override
    public void onCompletion(@NonNull final BrowseResult result) {
        mList = new ArrayList<>(result.getProgress());
        mEntryListener.onUpdate(mList, false);
    }

    @Override
    public void onProgressUpdate(@NonNull final BrowseResult result) {
        mList = new ArrayList<>(result.getProgress());
        mEntryListener.onUpdate(mList, true);
    }
}
