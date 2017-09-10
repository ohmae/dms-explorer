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
import net.mm2d.dmsexplorer.domain.model.ExploreListener;
import net.mm2d.dmsexplorer.domain.model.ExploreListenerAdapter;
import net.mm2d.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ContentDirectoryEntity implements DirectoryEntity {
    private static final String ROOT_OBJECT_ID = "0";
    private static final String ROOT_TITLE = "";
    @NonNull
    private final String mParentId;
    @NonNull
    private final String mParentTitle;
    @Nullable
    private ContentEntity mSelectedEntity;
    @NonNull
    private final List<ContentEntity> mList = new ArrayList<>();
    private static final ExploreListener ENTRY_LISTENER = new ExploreListenerAdapter();
    @NonNull
    private ExploreListener mEntryListener = ENTRY_LISTENER;
    private volatile boolean mInProgress = true;
    private volatile Disposable mDisposable;

    public ContentDirectoryEntity() {
        this(ROOT_OBJECT_ID, ROOT_TITLE);
    }

    private ContentDirectoryEntity(
            @NonNull final String parentId,
            @NonNull final String parentTitle) {
        mParentId = parentId;
        mParentTitle = parentTitle;
    }

    public void terminate() {
        setExploreListener(null);
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
    @Override
    public String getParentName() {
        return mParentTitle;
    }

    @Override
    public boolean isInProgress() {
        return mInProgress;
    }

    @NonNull
    @Override
    public List<ContentEntity> getEntities() {
        return mList;
    }

    @Nullable
    public ContentDirectoryEntity enterChild(@NonNull final ContentEntity entity) {
        if (!mList.contains(entity)) {
            return null;
        }
        if (entity.getType() != ContentType.CONTAINER) {
            return null;
        }
        mSelectedEntity = entity;
        final CdsObject object = (CdsObject) entity.getObject();
        return new ContentDirectoryEntity(object.getObjectId(), object.getTitle());
    }

    @Override
    public void setSelectedEntity(@Nullable final ContentEntity entity) {
        if (!mList.contains(entity)) {
            mSelectedEntity = null;
            return;
        }
        mSelectedEntity = entity;
    }

    @Nullable
    @Override
    public ContentEntity getSelectedEntity() {
        return mSelectedEntity;
    }

    public void setExploreListener(@Nullable final ExploreListener listener) {
        mEntryListener = listener != null ? listener : ENTRY_LISTENER;
    }

    public void clearState() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
        mSelectedEntity = null;
        mInProgress = true;
        mList.clear();
        mEntryListener.onStart();
    }

    public void startBrowse(@NonNull final Observable<CdsObject> observable) {
        mEntryListener.onStart();
        mDisposable = observable
                .subscribe(object -> {
                    mList.add(new CdsContentEntity(object));
                    mEntryListener.onUpdate(mList);
                }, Log::w, () -> {
                    mInProgress = false;
                    mEntryListener.onComplete();
                });
    }
}
