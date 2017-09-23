/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.domain.entity.ContentDirectoryEntity;
import net.mm2d.dmsexplorer.domain.entity.ContentEntity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MediaServerModel implements ExploreListener {
    public interface PlaybackTargetObserver {
        void update(ContentEntity entity);
    }

    public static final int SCAN_MODE_SEQUENTIAL = 0;
    public static final int SCAN_MODE_LOOP = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SCAN_MODE_SEQUENTIAL, SCAN_MODE_LOOP})
    public @interface ScanMode {
    }

    private static final String DELIMITER = " < ";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final PlaybackTargetObserver mPlaybackTargetObserver;
    private final MediaServer mMediaServer;
    private final LinkedList<ContentDirectoryEntity> mHistoryStack = new LinkedList<>();
    private String mPath;
    @NonNull
    private final static ExploreListener EXPLORE_LISTENER = new ExploreListenerAdapter();
    @NonNull
    private volatile ExploreListener mExploreListener = EXPLORE_LISTENER;

    public MediaServerModel(
            @NonNull final Context context,
            @NonNull final MediaServer server,
            @NonNull final PlaybackTargetObserver observer) {
        mMediaServer = server;
        mPlaybackTargetObserver = observer;
    }

    @NonNull
    public MediaServer getMediaServer() {
        return mMediaServer;
    }

    public void initialize() {
        prepareEntry(new ContentDirectoryEntity());
    }

    public void terminate() {
        setExploreListener(null);
        for (final ContentDirectoryEntity directory : mHistoryStack) {
            directory.terminate();
        }
        mHistoryStack.clear();
    }

    public boolean canDelete(@NonNull final ContentEntity entity) {
        if (!entity.canDelete()) {
            return false;
        }
        if (mHistoryStack.size() >= 2) {
            final ContentEntity p = mHistoryStack.get(1).getSelectedEntity();
            if (p != null && !p.canDelete()) {
                return false;
            }
        }
        return mMediaServer.hasDestroyObject();
    }

    public void delete(
            @NonNull final ContentEntity entity,
            @Nullable final Runnable successCallback,
            @Nullable final Runnable errorCallback) {
        final Runnable doNothing = () -> {
        };
        final Runnable onSuccess = successCallback == null ? doNothing : successCallback;
        final Runnable onError = errorCallback == null ? doNothing : errorCallback;
        final String id = ((CdsObject) entity.getObject()).getObjectId();
        mMediaServer.destroyObject(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == MediaServer.NO_ERROR) {
                        onSuccess.run();
                        reload();
                        return;
                    }
                    onError.run();
                }, throwable -> onError.run());
    }

    public boolean enterChild(@NonNull final ContentEntity entity) {
        final ContentDirectoryEntity directory = mHistoryStack.peekFirst();
        if (directory == null) {
            return false;
        }
        final ContentDirectoryEntity child = directory.enterChild(entity);
        if (child == null) {
            return false;
        }
        directory.setExploreListener(null);
        prepareEntry(child);
        updatePlaybackTarget();
        return true;
    }

    private void prepareEntry(@NonNull final ContentDirectoryEntity directory) {
        mHistoryStack.offerFirst(directory);
        mPath = makePath();
        directory.setExploreListener(this);
        directory.clearState();
        directory.startBrowse(mMediaServer.browse(directory.getParentId()));
    }

    public boolean exitToParent() {
        if (mHistoryStack.size() < 2) {
            return false;
        }
        mExploreListener.onStart();

        mHandler.post(() -> {
            final ContentDirectoryEntity directory = mHistoryStack.pollFirst();
            if (directory == null) {
                return;
            }
            directory.terminate();
            mPath = makePath();
            final ContentDirectoryEntity parent = mHistoryStack.peekFirst();
            if (parent == null) {
                return;
            }
            parent.setExploreListener(this);
            updatePlaybackTarget();
            mExploreListener.onUpdate(parent.getEntities());
            mExploreListener.onComplete();
        });
        return true;
    }

    public void reload() {
        final ContentDirectoryEntity directory = mHistoryStack.peekFirst();
        if (directory == null) {
            return;
        }
        directory.clearState();
        directory.startBrowse(mMediaServer.browse(directory.getParentId()));
    }

    public void setExploreListener(@Nullable final ExploreListener listener) {
        mExploreListener = listener != null ? listener : EXPLORE_LISTENER;
        final ContentDirectoryEntity directory = mHistoryStack.peekFirst();
        if (directory == null) {
            return;
        }
        mExploreListener.onStart();
        mExploreListener.onUpdate(directory.getEntities());
        if (!directory.isInProgress()) {
            mExploreListener.onComplete();
        }
    }

    public String getUdn() {
        return mMediaServer.getUdn();
    }

    public String getTitle() {
        return mMediaServer.getFriendlyName();
    }

    public String getPath() {
        return mPath;
    }

    public void setSelectedEntity(@NonNull final ContentEntity entity) {
        mHistoryStack.peekFirst().setSelectedEntity(entity);
        updatePlaybackTarget();
    }

    private void updatePlaybackTarget() {
        mPlaybackTargetObserver.update(getSelectedEntity());
    }

    public ContentEntity getSelectedEntity() {
        final ContentDirectoryEntity entry = mHistoryStack.peekFirst();
        if (entry == null) {
            return null;
        }
        return entry.getSelectedEntity();
    }

    public boolean selectPreviousEntity(@ScanMode final int scanMode) {
        final ContentEntity nextEntity = findPrevious(getSelectedEntity(), scanMode);
        if (nextEntity == null) {
            return false;
        }
        setSelectedEntity(nextEntity);
        return true;
    }

    @Nullable
    private ContentEntity findPrevious(
            @Nullable final ContentEntity current,
            @ScanMode final int scanMode) {
        final ContentDirectoryEntity directory = mHistoryStack.peekFirst();
        if (current == null || directory == null) {
            return null;
        }
        final List<ContentEntity> list = directory.getEntities();
        switch (scanMode) {
            case SCAN_MODE_SEQUENTIAL:
                return findPreviousSequential(current, list);
            case SCAN_MODE_LOOP:
                return findPreviousLoop(current, list);
        }
        return null;
    }

    @Nullable
    private ContentEntity findPreviousSequential(
            @NonNull final ContentEntity current,
            @NonNull final List<ContentEntity> list) {
        final int index = list.indexOf(current);
        if (index - 1 < 0) {
            return null;
        }
        for (int i = index - 1; i >= 0; i--) {
            final ContentEntity target = list.get(i);
            if (isValidEntity(current, target)) {
                return target;
            }
        }
        return null;
    }

    @Nullable
    private ContentEntity findPreviousLoop(
            @NonNull final ContentEntity current,
            @NonNull final List<ContentEntity> list) {
        final int size = list.size();
        final int index = list.indexOf(current);
        for (int i = (size + index - 1) % size; i != index; i = (size + i - 1) % size) {
            final ContentEntity target = list.get(i);
            if (isValidEntity(current, target)) {
                return target;
            }
        }
        return null;
    }

    public boolean selectNextEntity(@ScanMode final int scanMode) {
        final ContentEntity nextEntity = findNext(getSelectedEntity(), scanMode);
        if (nextEntity == null) {
            return false;
        }
        setSelectedEntity(nextEntity);
        return true;
    }

    @Nullable
    private ContentEntity findNext(
            @Nullable final ContentEntity current,
            @ScanMode final int scanMode) {
        if (mHistoryStack.isEmpty()) {
            return null;
        }
        final List<ContentEntity> list = mHistoryStack.peekFirst().getEntities();
        if (current == null || list == null) {
            return null;
        }
        switch (scanMode) {
            case SCAN_MODE_SEQUENTIAL:
                return findNextSequential(current, list);
            case SCAN_MODE_LOOP:
                return findNextLoop(current, list);
        }
        return null;
    }

    @Nullable
    private ContentEntity findNextSequential(
            @NonNull final ContentEntity current,
            @NonNull final List<ContentEntity> list) {
        final int size = list.size();
        final int index = list.indexOf(current);
        if (index + 1 == size) {
            return null;
        }
        for (int i = index + 1; i < size; i++) {
            final ContentEntity target = list.get(i);
            if (isValidEntity(current, target)) {
                return target;
            }
        }
        return null;
    }

    @Nullable
    private ContentEntity findNextLoop(
            @NonNull final ContentEntity current,
            @NonNull final List<ContentEntity> list) {
        final int size = list.size();
        final int index = list.indexOf(current);
        for (int i = (index + 1) % size; i != index; i = (i + 1) % size) {
            final ContentEntity target = list.get(i);
            if (isValidEntity(current, target)) {
                return target;
            }
        }
        return null;
    }

    private boolean isValidEntity(
            @NonNull final ContentEntity current,
            @NonNull final ContentEntity target) {
        return target.getType() == current.getType()
                && target.hasResource()
                && !target.isProtected();
    }

    @NonNull
    private String makePath() {
        final StringBuilder sb = new StringBuilder();
        for (final ContentDirectoryEntity directory : mHistoryStack) {
            if (sb.length() != 0 && directory.getParentName().length() != 0) {
                sb.append(DELIMITER);
            }
            sb.append(directory.getParentName());
        }
        return sb.toString();
    }

    @Override
    public void onStart() {
        mExploreListener.onStart();
    }

    @Override
    public void onUpdate(@NonNull final List<ContentEntity> list) {
        mExploreListener.onUpdate(list);
    }

    @Override
    public void onComplete() {
        mExploreListener.onComplete();
    }
}
