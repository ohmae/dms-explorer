/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.Application;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;

import net.mm2d.dmsexplorer.domain.AppRepository;
import net.mm2d.dmsexplorer.log.EventLogger;
import net.mm2d.dmsexplorer.settings.Settings;
import net.mm2d.dmsexplorer.util.update.UpdateChecker;
import net.mm2d.dmsexplorer.view.eventrouter.EventRouter;
import net.mm2d.log.AndroidLogInitializer;
import net.mm2d.log.Log;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Log出力変更のための継承。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.setInitializer(AndroidLogInitializer.get());
        Log.initialize(BuildConfig.DEBUG, true);
        setStrictMode();
        RxJavaPlugins.setErrorHandler(e -> Log.w(e instanceof UndeliverableException ? e.getCause() : e));
        Settings.initialize(this);
        EventRouter.initialize(this);
        EventLogger.initialize(this);
        Repository.set(new AppRepository(this));
        new UpdateChecker().check();
    }

    private void setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDropBox()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDropBox()
                    .build());
        } else {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX);
            StrictMode.setVmPolicy(VmPolicy.LAX);
        }
    }
}
