/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.upnp.cds;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Browse(BrowseMetadata)のResultを表現するFutureオブジェクト。
 *
 * <p>Browse実行によってこのオブジェクトが即座に返される。
 * Browseコマンド自体は非同期に実行され、このオブジェクトから結果を取り出す処理がブロックされる。
 * また、Futureのインターフェース以外に、コールバックを経由したイベントドリブンな結果取得も提供する。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class BrowseMetadataResult implements Future<CdsObject> {
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
        void onCompletion(@NonNull BrowseMetadataResult result);
    }

    @Nullable
    private Thread mThread;
    private boolean mDone;
    private boolean mCancelled;
    @Nullable
    private CdsObject mResult;
    @Nullable
    private StatusListener mListener;

    /**
     * インスタンス作成。
     *
     * <p>パッケージの外ではインスタンス作成禁止
     */
    BrowseMetadataResult() {
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
    public synchronized CdsObject get()
            throws InterruptedException, ExecutionException {
        while (!isDone()) {
            wait();
        }
        return mResult;
    }

    @Override
    @Nullable
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

    /**
     * コマンド実行完了及び結果を通知するリスナーを登録する。
     *
     * <p>このメソッドをコールした時点で完了していた場合は、
     * このスレッド上でonCompletion()がコールされたのち、処理が戻る。
     *
     * @param listener リスナー
     */
    public synchronized void setStatusListener(@Nullable StatusListener listener) {
        mListener = listener;
        if (isDone() && mListener != null) {
            mListener.onCompletion(this);
        }
    }

    /**
     * 結果を登録する。
     *
     * <p>結果が即座に取得できるようになるほか、
     * 結果取得待ちのスレッドへnotifyを行い、
     * リスナーが登録されていた場合はリスナー通知も行う。
     *
     * @param result BrowseMetadataの結果
     */
    synchronized void set(@Nullable CdsObject result) {
        mResult = result;
        mDone = true;
        notifyAll();
        if (mListener != null) {
            mListener.onCompletion(this);
        }
    }
}
