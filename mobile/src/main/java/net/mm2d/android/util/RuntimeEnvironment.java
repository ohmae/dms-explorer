package net.mm2d.android.util;

import android.os.Build;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class RuntimeEnvironment {

    /**
     * 現在の実行状態がエミュレータか否かを判定する。
     *
     * @return true:エミュレータである。false:それ以外
     */
    public static boolean isEmulator() {
        return Build.UNKNOWN.equals(Build.BOOTLOADER)
                && Build.MODEL.contains("Android SDK");
    }
}
