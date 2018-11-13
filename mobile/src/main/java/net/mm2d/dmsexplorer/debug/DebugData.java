package net.mm2d.dmsexplorer.debug;

import android.content.Context;

import net.mm2d.android.util.RuntimeEnvironment;
import net.mm2d.dmsexplorer.BuildConfig;
import net.mm2d.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import okio.Okio;

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
public class DebugData {
    private static List<String> sPinnedDeviceLocationList = Collections.emptyList();

    public static void initialize(@NonNull final Context context) {
        if (!BuildConfig.DEBUG || !RuntimeEnvironment.isEmulator()) {
            return;
        }
        try {
            final InputStream is = context.getAssets().open("locations.json");
            final String json = Okio.buffer(Okio.source(is)).readUtf8();
            final JSONObject object = new JSONObject(json);
            final List<String> list = new ArrayList<>();
            for (final Iterator<String> i = object.keys(); i.hasNext(); ) {
                list.add(object.getString(i.next()));
            }
            Log.e(list.toString());
            sPinnedDeviceLocationList = list;
        } catch (final IOException ignored) {
        } catch (final JSONException ignored) {
        }
    }

    @NonNull
    public static List<String> getPinnedDeviceLocationList() {
        return sPinnedDeviceLocationList;
    }
}
