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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class MediaServerModel implements ExploreListener {
    public interface PlaybackTargetObserver {
        void update(CdsObject object);
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
    private final Deque<ContentDirectoryEntity> mHistoryStack = new LinkedList<>();
    private String mPath;
    private final static ExploreListener EXPLORE_LISTENER = new ExploreListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onUpdate(@NonNull final List<CdsObject> list) {
        }

        @Override
        public void onComplete() {
        }
    };
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

    public boolean enterChild(@NonNull final CdsObject object) {
        final ContentDirectoryEntity directory = mHistoryStack.peekFirst();
        if (directory == null) {
            return false;
        }
        final ContentDirectoryEntity child = directory.enterChild(object);
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
            mExploreListener.onUpdate(parent.getList());
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
        mExploreListener.onStart();
        mExploreListener.onUpdate(directory.getList());
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

    public void setSelectedObject(@NonNull final CdsObject object) {
        mHistoryStack.peekFirst().setSelectedObject(object);
        updatePlaybackTarget();
    }

    private void updatePlaybackTarget() {
        mPlaybackTargetObserver.update(getSelectedObject());
    }

    public CdsObject getSelectedObject() {
        final ContentDirectoryEntity entry = mHistoryStack.peekFirst();
        if (entry == null) {
            return null;
        }
        return entry.getSelectedObject();
    }

    public boolean selectPreviousObject(@ScanMode final int scanMode) {
        final CdsObject nextObject = findPrevious(getSelectedObject(), scanMode);
        if (nextObject == null) {
            return false;
        }
        setSelectedObject(nextObject);
        return true;
    }

    @Nullable
    private CdsObject findPrevious(
            @Nullable final CdsObject current,
            @ScanMode final int scanMode) {
        final ContentDirectoryEntity directory = mHistoryStack.peekFirst();
        if (current == null || directory == null) {
            return null;
        }
        final List<CdsObject> list = directory.getList();
        switch (scanMode) {
            case SCAN_MODE_SEQUENTIAL:
                return findPreviousSequential(current, list);
            case SCAN_MODE_LOOP:
                return findPreviousLoop(current, list);
        }
        return null;
    }

    @Nullable
    private CdsObject findPreviousSequential(
            @NonNull final CdsObject current,
            @NonNull final List<CdsObject> list) {
        final int index = list.indexOf(current);
        if (index - 1 < 0) {
            return null;
        }
        for (int i = index - 1; i >= 0; i--) {
            final CdsObject target = list.get(i);
            if (isValidObject(current, target)) {
                return target;
            }
        }
        return null;
    }

    @Nullable
    private CdsObject findPreviousLoop(
            @NonNull final CdsObject current,
            @NonNull final List<CdsObject> list) {
        final int size = list.size();
        final int index = list.indexOf(current);
        for (int i = (size + index - 1) % size; i != index; i = (size + i - 1) % size) {
            final CdsObject target = list.get(i);
            if (isValidObject(current, target)) {
                return target;
            }
        }
        return null;
    }

    public boolean selectNextObject(@ScanMode final int scanMode) {
        final CdsObject nextObject = findNext(getSelectedObject(), scanMode);
        if (nextObject == null) {
            return false;
        }
        setSelectedObject(nextObject);
        return true;
    }

    @Nullable
    private CdsObject findNext(
            @Nullable final CdsObject current,
            @ScanMode final int scanMode) {
        if (mHistoryStack.isEmpty()) {
            return null;
        }
        final List<CdsObject> list = mHistoryStack.peekFirst().getList();
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
    private CdsObject findNextSequential(
            @NonNull final CdsObject current,
            @NonNull final List<CdsObject> list) {
        final int size = list.size();
        final int index = list.indexOf(current);
        if (index + 1 == size) {
            return null;
        }
        for (int i = index + 1; i < size; i++) {
            final CdsObject target = list.get(i);
            if (isValidObject(current, target)) {
                return target;
            }
        }
        return null;
    }

    @Nullable
    private CdsObject findNextLoop(
            @NonNull final CdsObject current,
            @NonNull final List<CdsObject> list) {
        final int size = list.size();
        final int index = list.indexOf(current);
        for (int i = (index + 1) % size; i != index; i = (i + 1) % size) {
            final CdsObject target = list.get(i);
            if (isValidObject(current, target)) {
                return target;
            }
        }
        return null;
    }

    private boolean isValidObject(
            @NonNull final CdsObject current,
            @NonNull final CdsObject target) {
        return target.getType() == current.getType()
                && target.hasResource()
                && !target.hasProtectedResource();
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
    public void onUpdate(@NonNull final List<CdsObject> list) {
        mExploreListener.onUpdate(list);
    }

    @Override
    public void onComplete() {
        mExploreListener.onComplete();
    }
}
