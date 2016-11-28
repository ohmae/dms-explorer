/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer;

import android.app.Application;

import net.mm2d.util.Log;
import net.mm2d.util.Log.Print;

/**
 * Log出力変更のための継承。
 *
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class App extends Application {
    /**
     * デバッグログの出力方法。
     */
    private static class AndroidPrint implements Print {
        @Override
        public void println(int level, String tag, String message) {
            switch (level) {
                default:
                case Log.VERBOSE:
                    android.util.Log.v(tag, message);
                    break;
                case Log.DEBUG:
                    android.util.Log.d(tag, message);
                    break;
                case Log.INFO:
                    android.util.Log.i(tag, message);
                    break;
                case Log.WARN:
                    android.util.Log.w(tag, message);
                    break;
                case Log.ERROR:
                    android.util.Log.e(tag, message);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.setPrint(new AndroidPrint());
    }
}
