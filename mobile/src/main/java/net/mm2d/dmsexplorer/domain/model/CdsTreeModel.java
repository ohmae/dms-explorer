/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.mm2d.android.upnp.cds.CdsObject;
import net.mm2d.android.upnp.cds.MediaServer;
import net.mm2d.dmsexplorer.domain.entity.CdsTreeDirectory;
import net.mm2d.dmsexplorer.domain.entity.CdsTreeDirectory.EntryListener;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class CdsTreeModel implements EntryListener {
    private static final String DELIMITER = " < ";
    private final MediaServer mMediaServer;
    private final Deque<CdsTreeDirectory> mHistoryStack = new LinkedList<>();
    private String mPath;
    private CdsListListener CDS_LIST_LISTENER = (list, inProgress) -> {
    };
    private volatile CdsListListener mCdsListListener = CDS_LIST_LISTENER;

    public interface CdsListListener {
        void onUpdateList(@NonNull List<CdsObject> list, boolean inProgress);
    }

    public CdsTreeModel(MediaServer server) {
        mMediaServer = server;
    }

    public void initialize() {
        prepareEntry(new CdsTreeDirectory());
    }

    public boolean enterChild(@NonNull final CdsObject object) {
        final CdsTreeDirectory directory = mHistoryStack.peekFirst();
        final CdsTreeDirectory child = directory.enterChild(object);
        if (child == null) {
            return false;
        }
        directory.setEntryListener(null);
        prepareEntry(child);
        return true;
    }

    private void prepareEntry(CdsTreeDirectory directory) {
        mHistoryStack.offerFirst(directory);
        mPath = makePath();
        directory.setEntryListener(this);
        mCdsListListener.onUpdateList(Collections.emptyList(), true);
        directory.clearState();
        directory.setBrowseResult(mMediaServer.browse(directory.getParentId()));
    }

    public boolean exitToParent() {
        if (mHistoryStack.size() <= 1) {
            return false;
        }
        mCdsListListener.onUpdateList(Collections.emptyList(), true);
        final CdsTreeDirectory directory = mHistoryStack.pollFirst();
        directory.terminate();
        mPath = makePath();
        final CdsTreeDirectory parent = mHistoryStack.peekFirst();
        parent.setEntryListener(this);
        mCdsListListener.onUpdateList(parent.getList(), parent.isInProgress());
        return true;
    }

    public void reload() {
        mCdsListListener.onUpdateList(Collections.emptyList(), true);
        final CdsTreeDirectory directory = mHistoryStack.peekFirst();
        directory.clearState();
        directory.setBrowseResult(mMediaServer.browse(directory.getParentId()));
    }

    public void terminate() {
        setCdsListListener(null);
        for (CdsTreeDirectory directory : mHistoryStack) {
            directory.terminate();
        }
        mHistoryStack.clear();
    }

    public void setCdsListListener(@Nullable CdsListListener listener) {
        mCdsListListener = listener != null ? listener : CDS_LIST_LISTENER;
        final CdsTreeDirectory directory = mHistoryStack.peekFirst();
        mCdsListListener.onUpdateList(directory.getList(), directory.isInProgress());
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
    }

    public CdsObject getSelectedObject() {
        return mHistoryStack.peekFirst().getSelectedObject();
    }

    private String makePath() {
        final StringBuilder sb = new StringBuilder();
        for (final CdsTreeDirectory directory : mHistoryStack) {
            if (sb.length() != 0 && directory.getParentTitle().length() != 0) {
                sb.append(DELIMITER);
            }
            sb.append(directory.getParentTitle());
        }
        return sb.toString();
    }

    @Override
    public void onUpdate(@NonNull final List<CdsObject> list, final boolean inProgress) {
        mCdsListListener.onUpdateList(list, inProgress);
    }
}
