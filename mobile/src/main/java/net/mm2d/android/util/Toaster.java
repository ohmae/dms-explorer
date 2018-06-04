package net.mm2d.android.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Toaster {
    @Nullable
    public static Toast show(
            @Nullable final Context context,
            @StringRes final int resId) {
        if (context == null) {
            return null;
        }
        final Toast toast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        toast.show();
        return toast;
    }
}
