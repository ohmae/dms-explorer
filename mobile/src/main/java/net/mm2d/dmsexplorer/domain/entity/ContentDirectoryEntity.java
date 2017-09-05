/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ContentDirectoryEntity {
    private static final String ROOT_OBJECT_ID = "0";
    private static final String ROOT_TITLE = "";
    @NonNull
    private final String mParentId;
    @NonNull
    private final String mParentTitle;
    @Nullable
    private CdsObject mSelectedObject;
    @NonNull
    private final List<CdsObject> mList = new ArrayList<>();
    private static final EntryListener ENTRY_LISTENER = new EntryListener() {
        @Override
        public void onUpdateState(final boolean inProgress) {
        }

        @Override
        public void onUpdateList(@NonNull final List<CdsObject> list) {
        }
    };
    @NonNull
    private EntryListener mEntryListener = ENTRY_LISTENER;
    private volatile boolean mInProgress = true;
    private volatile Disposable mDisposable;

    public interface EntryListener {
        void onUpdateState(boolean inProgress);

        void onUpdateList(@NonNull List<CdsObject> list);
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
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
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
        return mInProgress;
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
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
        mSelectedObject = null;
        mInProgress = true;
        mList.clear();
        mEntryListener.onUpdateList(mList);
        mEntryListener.onUpdateState(true);
    }

    public void startBrowse(@NonNull final Observable<CdsObject> observable) {
        mDisposable = observable
                .buffer(200L, TimeUnit.MILLISECONDS, 50)
                .subscribe(object -> {
            mList.addAll(object);
            mEntryListener.onUpdateList(mList);
        }, Log::w, () -> {
            mInProgress = false;
            mEntryListener.onUpdateState(false);
        });
    }
}
