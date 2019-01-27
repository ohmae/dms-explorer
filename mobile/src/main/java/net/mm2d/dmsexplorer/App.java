/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;

import net.mm2d.dmsexplorer.debug.DebugData;
import net.mm2d.dmsexplorer.domain.AppRepository;
import net.mm2d.dmsexplorer.log.EventLogger;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.update.UpdateChecker;
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter;
import net.mm2d.log.Logger;
import net.mm2d.log.android.AndroidSenders;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Log出力変更のための継承。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Logger.setLogLevel(Logger.VERBOSE);
            Logger.setSender(AndroidSenders.create());
            AndroidSenders.appendCaller(true);
            AndroidSenders.appendThread(true);
        }
        setStrictMode();
        RxJavaPlugins.setErrorHandler(this::logError);
        DebugData.initialize(this);
        Settings.initialize(this);
        EventRouter.initialize(this);
        EventLogger.initialize(this);
        EventLogger.sendDailyLog();
        Repository.set(new AppRepository(this));
        new UpdateChecker().check();
    }

    private void logError(@NonNull final Throwable e) {
        if (e instanceof UndeliverableException) {
            Logger.w("UndeliverableException:", e.getCause());
        } else if (e instanceof OnErrorNotImplementedException) {
            Logger.w("OnErrorNotImplementedException:", e.getCause());
        } else {
            Logger.w(e);
        }
    }

    private void setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        } else {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX);
            StrictMode.setVmPolicy(VmPolicy.LAX);
        }
    }
}
