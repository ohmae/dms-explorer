/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class BrowseResult implements Future<List<CdsObject>> {
    public interface StatusListener {
        void onCompletion(List<CdsObject> result);

        void onProgressUpdate(List<CdsObject> result);
    }

    private Thread mThread;
    private boolean mDone;
    private boolean mCancelled;
    private List<CdsObject> mResult;
    private StatusListener mListener;
    private final String mObjectId;
    private final String mFilter;
    private final String mSortCriteria;
    private final int mStartingIndex;
    private final int mRequestedCount;

    BrowseResult(String objectId, String filter, String sortCriteria,
                 int startingIndex, int requestedCount) {
        mObjectId = objectId;
        mFilter = filter;
        mSortCriteria = sortCriteria;
        mStartingIndex = startingIndex;
        mRequestedCount = requestedCount;
    }

    String getObjectId() {
        return mObjectId;
    }

    String getFilter() {
        return mFilter;
    }

    String getSortCriteria() {
        return mSortCriteria;
    }

    int getStartingIndex() {
        return mStartingIndex;
    }

    int getRequestedCount() {
        return mRequestedCount;
    }

    synchronized void setThread(Thread thread) {
        mThread = thread;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return true;
        }
        mCancelled = true;
        if (mayInterruptIfRunning && mThread != null) {
            mThread.interrupt();
        }
        return isDone();
    }

    @Override
    public synchronized boolean isCancelled() {
        return mCancelled;
    }

    @Override
    public synchronized boolean isDone() {
        return mDone;
    }

    @Override
    public synchronized List<CdsObject> get()
            throws InterruptedException, ExecutionException {
        while (!isDone()) {
            wait();
        }
        return mResult;
    }

    @Override
    public synchronized List<CdsObject> get(long timeout, @NonNull TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (!isDone()) {
            wait(unit.toMillis(timeout));
        }
        if (!isDone()) {
            throw new TimeoutException();
        }
        return mResult;
    }

    public synchronized void setStatusListener(StatusListener listener) {
        mListener = listener;
        if (!isCancelled() && isDone() && mListener != null) {
            mListener.onCompletion(mResult);
        }
    }

    protected synchronized void set(List<CdsObject> result) {
        mResult = result;
        mDone = true;
        notifyAll();
        if (!isCancelled() && mListener != null) {
            mListener.onCompletion(mResult);
        }
    }

    protected synchronized void setProgress(List<CdsObject> result) {
        mResult = result;
        if (!isCancelled() && mListener != null) {
            mListener.onProgressUpdate(mResult);
        }
    }
}
