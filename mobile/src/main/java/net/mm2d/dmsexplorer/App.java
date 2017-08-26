/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.Application;

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
        if (BuildConfig.DEBUG) {
            Log.setAppendCaller(true);
            Log.setLogLevel(Log.VERBOSE);
            Log.setPrint((level, tag, message) -> {
                final String[] lines = message.split("\n");
                for (final String line : lines) {
                    android.util.Log.println(level, tag, line);
                }
            });
            return;
        }
        Log.setLogLevel(Log.ASSERT);
        Log.setPrint((level, tag, message) -> {
        });
    }
}
