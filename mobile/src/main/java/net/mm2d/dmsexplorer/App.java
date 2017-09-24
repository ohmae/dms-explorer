/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import net.mm2d.dmsexplorer.domain.AppRepository;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.update.UpdateChecker;
import net.mm2d.util.Log;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Log出力変更のための継承。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setUpDebugLog();
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                Log.w(e.getCause());
                return;
            }
            Log.w(e);
        });
        Settings.initialize(this);
        EventLogger.initialize(this);
        Repository.set(new AppRepository(this));
        new UpdateChecker(this).check();
    }

    private void setUpDebugLog() {
        if (!BuildConfig.DEBUG) {
            Log.setLogLevel(Log.ASSERT);
            Log.setPrint(Log.EMPTY_PRINT);
            return;
        }
        final Looper mainLooper = Looper.getMainLooper();
        final Thread mainThread = mainLooper.getThread();
        final Handler handler = new Handler(mainLooper);
        Log.setAppendCaller(true);
        Log.setLogLevel(Log.VERBOSE);
        Log.setPrint((level, tag, message) -> {
            if (Thread.currentThread() == mainThread) {
                println(level, tag, message);
                return;
            }
            handler.post(() -> println(level, tag, message));
        });
    }

    private void println(
            int level,
            @NonNull String tag,
            @NonNull String message) {
        final String[] lines = message.split("\n");
        for (final String line : lines) {
            android.util.Log.println(level, tag, line);
        }
    }
}
