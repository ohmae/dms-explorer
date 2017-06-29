package net.mm2d.android.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class Toaster {
    private Toaster() {
        throw new AssertionError();
    }

    @NonNull
    public static Toast showLong(@NonNull final Context context, @StringRes final int resId) {
        return show(context, resId, Toast.LENGTH_LONG);
    }

    @NonNull
    public static Toast showLong(@NonNull final Context context, @NonNull final CharSequence text) {
        return show(context, text, Toast.LENGTH_LONG);
    }

    @NonNull
    public static Toast showShort(@NonNull final Context context, @StringRes final int resId) {
        return show(context, resId, Toast.LENGTH_SHORT);
    }

    @NonNull
    public static Toast showShort(@NonNull final Context context, @NonNull final CharSequence text) {
        return show(context, text, Toast.LENGTH_SHORT);
    }

    @NonNull
    private static Toast show(@NonNull final Context context, @StringRes final int resId, final int length) {
        return show(context, context.getText(resId), length);
    }

    @NonNull
    private static Toast show(@NonNull final Context context, @NonNull final CharSequence text, final int length) {
        final Toast toast = Toast.makeText(context, text, length);
        toast.show();
        return toast;
    }
}
