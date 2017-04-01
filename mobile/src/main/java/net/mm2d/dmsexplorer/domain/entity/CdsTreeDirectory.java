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
    private static final int NOT_SELECTED = -1;
    private final String mParentId;
    private final String mParentTitle;
    private int mSelectedPosition;
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
        mSelectedPosition = NOT_SELECTED;
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

    public CdsTreeDirectory enterChild(int position) {
        if (position < 0 || position >= mList.size()) {
            return null;
        }
        final CdsObject object = mList.get(position);
        if (!object.isContainer()) {
            return null;
        }
        mSelectedPosition = position;
        return new CdsTreeDirectory(object.getObjectId(), object.getTitle());
    }

    public void select(int position) {
        if (position < 0 || position >= mList.size()) {
            mSelectedPosition = NOT_SELECTED;
            return;
        }
        mSelectedPosition = position;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public CdsObject getSelectedObject() {
        if (mSelectedPosition < 0) {
            return null;
        }
        return mList.get(mSelectedPosition);
    }

    public void setEntryListener(@Nullable EntryListener listener) {
        mEntryListener = listener != null ? listener : ENTRY_LISTENER;
    }

    public void clearState() {
        mSelectedPosition = NOT_SELECTED;
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
