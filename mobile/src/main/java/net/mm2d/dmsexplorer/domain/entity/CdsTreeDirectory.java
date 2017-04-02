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

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CdsTreeDirectory implements StatusListener {
    private static final String ROOT_OBJECT_ID = "0";
    private static final String ROOT_TITLE = "";
    private final String mParentId;
    private final String mParentTitle;
    private CdsObject mSelectedObject;
    private List<CdsObject> mList = Collections.emptyList();
    private BrowseResult mBrowseResult;
    private static final EntryListener ENTRY_LISTENER = (result, inProgress) -> {
    };
    private EntryListener mEntryListener = ENTRY_LISTENER;

    public interface EntryListener {
        void onUpdate(@NonNull List<CdsObject> list, boolean inProgress);
    }

    public CdsTreeDirectory() {
        this(ROOT_OBJECT_ID, ROOT_TITLE);
    }

    public CdsTreeDirectory(@NonNull String parentId, @NonNull String parentTitle) {
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

    public String getParentId() {
        return mParentId;
    }

    public String getParentTitle() {
        return mParentTitle;
    }

    public boolean isInProgress() {
        return mBrowseResult == null || !mBrowseResult.isDone();
    }

    public List<CdsObject> getList() {
        return mList;
    }

    public CdsTreeDirectory enterChild(@NonNull final CdsObject object) {
        if (!mList.contains(object)) {
            return null;
        }
        if (!object.isContainer()) {
            return null;
        }
        mSelectedObject = object;
        return new CdsTreeDirectory(object.getObjectId(), object.getTitle());
    }

    public void setSelectedObject(final CdsObject object) {
        if (!mList.contains(object)) {
            mSelectedObject = null;
        }
        mSelectedObject = object;
    }

    public CdsObject getSelectedObject() {
        return mSelectedObject;
    }

    public void setEntryListener(@Nullable EntryListener listener) {
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

    public void setBrowseResult(BrowseResult result) {
        mBrowseResult = result;
        mBrowseResult.setStatusListener(this);
    }

    @Override
    public void onCompletion(@NonNull final BrowseResult result) {
        mList = result.getProgress();
        mEntryListener.onUpdate(mList, false);
    }

    @Override
    public void onProgressUpdate(@NonNull final BrowseResult result) {
        mList = result.getProgress();
        mEntryListener.onUpdate(mList, true);
    }
}
