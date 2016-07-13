/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.cds;

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class BrowseMetadataResult implements Future<CdsObject> {
    public interface StatusListener {
        void onCompletion(CdsObject result);
    }

    private Thread mThread;
    private boolean mDone;
    private boolean mCancelled;
    private CdsObject mResult;
    private StatusListener mListener;
    private final String mObjectId;
    private final String mFilter;

    BrowseMetadataResult(String objectId, String filter) {
        mObjectId = objectId;
        mFilter = filter;
    }

    String getObjectId() {
        return mObjectId;
    }

    String getFilter() {
        return mFilter;
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
    public synchronized CdsObject get()
            throws InterruptedException, ExecutionException {
        while (!isDone()) {
            wait();
        }
        return mResult;
    }

    @Override
    public synchronized CdsObject get(long timeout, @NonNull TimeUnit unit)
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
        if (isDone() && mListener != null) {
            mListener.onCompletion(mResult);
        }
    }

    protected synchronized void set(CdsObject result) {
        mResult = result;
        mDone = true;
        notifyAll();
        if (mListener != null) {
            mListener.onCompletion(mResult);
        }
    }
}
