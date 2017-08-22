/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Browse(BrowseDirectChildren)のResultを表現するFutureオブジェクト。
 *
 * <p>Browse実行によってこのオブジェクトが即座に返される。
 * Browseコマンド自体は非同期に実行され、このオブジェクトから結果を取り出す処理がブロックされる。
 * また、Futureのインターフェース以外に、コールバックを経由したイベントドリブンな結果取得も提供する。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class BrowseResult implements Future<List<CdsObject>> {
    /**
     * 進捗状態通知のリスナー
     */
    public interface StatusListener {
        /**
         * 取得完了時にコールされる。
         *
         * <p>ネットワーク処理のスレッド上からコールされるため、
         * このメソッド内でブロック動作はさせないこと。
         * このメソッド内、及びこのメソッドがコールされたあとは、
         * {@link #get()}がブロックされずに結果を取得でき、
         * Exceptionも発生しない。
         *
         * @param result 結果
         * @see #get()
         */
        void onCompletion(@NonNull BrowseResult result);

        /**
         * 部分的に取得更新が行われたときにコールされる。
         *
         * <p>ネットワーク処理のスレッド上からコールされるため、
         * このメソッド内でブロック動作はさせないこと。
         * この時点では{@link #get()}をコールしても結果を取得できずブロック動作となる。
         * 途中結果を取得する場合はgetProgress()をコールすること。
         *
         * @param result 結果
         * @see #onProgressUpdate(BrowseResult)
         */
        void onProgressUpdate(@NonNull BrowseResult result);
    }

    @Nullable
    private Thread mThread;
    private boolean mDone;
    private boolean mCancelled;
    @Nullable
    private List<CdsObject> mResult;
    @NonNull
    private List<CdsObject> mProgress = Collections.emptyList();
    @Nullable
    private StatusListener mListener;

    /**
     * インスタンス作成。
     *
     * <p>パッケージの外ではインスタンス作成禁止
     */
    BrowseResult() {
    }

    /**
     * 割り込みを行うスレッドを登録する。
     *
     * @param thread 実行スレッド
     */
    synchronized void setThread(@Nullable Thread thread) {
        mThread = thread;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        }
        mCancelled = true;
        if (mayInterruptIfRunning && mThread != null) {
            mThread.interrupt();
        }
        return true;
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
    @Nullable
    public synchronized List<CdsObject> get()
            throws InterruptedException, ExecutionException {
        while (!isDone()) {
            wait();
        }
        return mResult;
    }

    @Override
    @Nullable
    public synchronized List<CdsObject> get(
            long timeout,
            @NonNull TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (!isDone()) {
            wait(unit.toMillis(timeout));
        }
        if (!isDone()) {
            throw new TimeoutException();
        }
        return mResult;
    }

    /**
     * 進捗状態を通知するリスナーを登録する。
     *
     * <p>このメソッドをコールした時点で完了していた場合は、
     * このスレッド上でonCompletion()がコールされたのち、処理が戻る。
     *
     * @param listener リスナー
     */
    public synchronized void setStatusListener(@Nullable StatusListener listener) {
        mListener = listener;
        if (!isCancelled() && isDone() && mListener != null) {
            mListener.onCompletion(this);
        }
    }

    /**
     * 現在までに取得できている途中結果を返す。
     *
     * <p>今後更新される情報のためunmodifiableListとして返す。
     * 途中経過がない場合は空のListとなり、nullにはならない。
     *
     * @return 途中結果
     */
    @NonNull
    public synchronized List<CdsObject> getProgress() {
        if (isDone()) {
            if (mResult == null) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(mResult);
        }
        return Collections.unmodifiableList(mProgress);
    }

    /**
     * 結果を登録する。
     *
     * <p>結果が即座に取得できるようになるほか、
     * 結果取得待ちのスレッドへnotifyを行い、
     * リスナーが登録されていた場合はリスナー通知も行う。
     *
     * @param result BrowseDirectChildrenの結果
     */
    synchronized void set(@Nullable List<CdsObject> result) {
        mResult = result;
        mDone = true;
        notifyAll();
        if (!isCancelled() && mListener != null) {
            mListener.onCompletion(this);
        }
    }

    /**
     * 結果取得の進捗を通知する。
     *
     * <p>このメソッドコールでは完了状態にならないため、
     * 結果取得待ちのスレッドは待たされたままである。
     *
     * @param progress 取得できているBrowseDirectChildrenの結果
     */
    synchronized void setProgress(@NonNull List<CdsObject> progress) {
        mProgress = progress;
        if (!isCancelled() && mListener != null) {
            mListener.onProgressUpdate(this);
        }
    }
}
